package com.InfraDesk.service;

import com.InfraDesk.config.WebClientConfig;
import com.InfraDesk.entity.MailIntegration;
import com.InfraDesk.entity.TicketingDepartmentConfig;
import com.InfraDesk.repository.MailIntegrationRepository;
import com.InfraDesk.repository.TicketingDepartmentConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MailFetchService {
    private final MailIntegrationRepository repo;
    private final MailAuthService authService;
    private final MailProcessorService processor; // see below
    private final WebClient webClient;

//    private final TicketingDepartmentConfigRepository ticketingDepartmentConfigRepository;
    private static final Logger log = LoggerFactory.getLogger(MailFetchService.class);

    @Scheduled(fixedDelayString = "${mail.poll.interval:30000}")
    public void pollAll() {
        List<MailIntegration> list = repo.findByEnabledTrue();
        for (MailIntegration m : list) {
            try {
                pollIntegration(m);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void pollIntegration(MailIntegration m) {

        m = authService.refreshIfNeeded(m);
        String accessToken = authService.getDecryptedAccessToken(m); // implement getDecryptedAccessToken decryptor helper
        if ("GMAIL".equalsIgnoreCase(m.getProvider())) {
            pollGmail(m, accessToken);
        } else if ("MICROSOFT".equalsIgnoreCase(m.getProvider())) {
            pollMicrosoft(m, accessToken);
        } else if ("IMAP".equalsIgnoreCase(m.getProvider())) {
            // implement IMAP polling fallback if tenant provided IMAP creds
        }
        m.setLastSyncAt(Instant.now());
        repo.save(m);
    }

    private void pollGmail(MailIntegration m, String accessToken) {

        // Build final query string before lambda
        String baseQuery = "in:inbox is:unread";

        // Example: after yesterday
        LocalDate date = LocalDate.now().minusDays(2);
        long epochSeconds = date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();

        String finalQuery = baseQuery + " after:" + epochSeconds; // <- effectively final

        Map resp =null;
        try {
             resp = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("gmail.googleapis.com")
                            .path("/gmail/v1/users/me/messages")
                            .queryParam("q", finalQuery)   // <--- pass RAW query, WebClient encodes safely
                            .build()
                    )
                    .headers(h -> h.setBearerAuth(accessToken))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

        } catch (WebClientResponseException e) {
            log.error("Gmail API error: Status = {}, Body = {}", e.getRawStatusCode(), e.getResponseBodyAsString());
            throw e;
        }

        if (resp == null) return;
        List<Map<String,Object>> messages = (List<Map<String,Object>>) resp.get("messages");
        if (messages == null) return;
        for (Map msg : messages) {
            String id = (String) msg.get("id");
            Map full = webClient.get()
                    .uri("https://gmail.googleapis.com/gmail/v1/users/me/messages/" + id + "?format=full")
                    .headers(h -> h.setBearerAuth(accessToken))
                    .retrieve().bodyToMono(Map.class).block();
            processor.processGmailMessage(full, m);
            // optionally mark message as read via modify
//            webClient.post()
//                    .uri("https://gmail.googleapis.com/gmail/v1/users/me/messages/" + id + "/modify")
//                    .headers(h -> h.setBearerAuth(accessToken))
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .bodyValue(Map.of("removeLabelIds", List.of("UNREAD")))
//                    .retrieve().bodyToMono(Void.class).block();

            try {
                webClient.post()
                        .uri("https://gmail.googleapis.com/gmail/v1/users/me/messages/" + id + "/modify")
                        .headers(h -> h.setBearerAuth(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(Map.of("removeLabelIds", List.of("UNREAD")))
                        .retrieve()
                        .bodyToMono(Void.class)
                        .block();
                log.info("Message {} marked as read.", id);
            } catch (Exception e) {
                log.error("Failed to mark message as read: {}", id, e);
            } finally {
                // Mark message as read regardless of success or failure
                try {
                    webClient.post()
                            .uri("https://gmail.googleapis.com/gmail/v1/users/me/messages/" + id + "/modify")
                            .headers(h -> h.setBearerAuth(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of("removeLabelIds", List.of("UNREAD")))
                            .retrieve()
                            .bodyToMono(Void.class)
                            .block();

                    log.info("Marked message {} as read", id);
                } catch (Exception e) {
                    log.error("Failed to mark message as read: {}", id, e);
                }
            }

        }
    }

    private void pollMicrosoft(MailIntegration m, String accessToken) {

        Map resp = webClient.get()
                .uri("https://graph.microsoft.com/v1.0/me/mailFolders('Inbox')/messages?$filter=isRead eq false")
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        List<Map> values = (List<Map>) resp.get("value");
        if (values == null) return;
        for (Map msg : values) {
            String id = (String) msg.get("id");
            Map full = webClient.get()
                    .uri("https://graph.microsoft.com/v1.0/me/messages/" + id)
                    .headers(h -> h.setBearerAuth(accessToken))
                    .retrieve().bodyToMono(Map.class).block();
            processor.processGraphMessage(full, m);
            // mark as read
            webClient.patch()
                    .uri("https://graph.microsoft.com/v1.0/me/messages/" + id)
                    .headers(h -> h.setBearerAuth(accessToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("isRead", true))
                    .retrieve().bodyToMono(Void.class).block();
        }
    }

}

