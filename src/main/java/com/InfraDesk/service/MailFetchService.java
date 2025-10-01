package com.InfraDesk.service;

import com.InfraDesk.entity.MailIntegration;
import com.InfraDesk.repository.MailIntegrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MailFetchService {
    private final MailIntegrationRepository repo;
    private final MailAuthService authService;
    private final MailProcessorService processor; // see below
    private final WebClient webClient = WebClient.create();
    private static final Logger log = LoggerFactory.getLogger(MailFetchService.class);


    @Scheduled(fixedDelayString = "${mail.poll.interval:30000}")
    public void pollAll() {
//        log.info("Polling Started");
        List<MailIntegration> list = repo.findByEnabledTrue();
//        log.info("Mail integrations found: " + list.size());
        for (MailIntegration m : list) {
            try {
                pollIntegration(m);
            } catch (Exception e) {
                // log and continue
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
        Map resp =null;
        try {
           resp = webClient.get()
                    .uri("https://gmail.googleapis.com/gmail/v1/users/me/messages?q=in:inbox is:unread")
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
            webClient.post()
                    .uri("https://gmail.googleapis.com/gmail/v1/users/me/messages/" + id + "/modify")
                    .headers(h -> h.setBearerAuth(accessToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("removeLabelIds", List.of("UNREAD")))
                    .retrieve().bodyToMono(Void.class).block();
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

