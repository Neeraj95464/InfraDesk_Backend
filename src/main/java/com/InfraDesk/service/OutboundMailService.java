package com.InfraDesk.service;

import com.InfraDesk.component.SimpleEncryptor;
import com.InfraDesk.entity.MailIntegration;
import com.InfraDesk.entity.Ticket;
import com.InfraDesk.entity.TicketMessage;
import com.InfraDesk.repository.TicketMessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.Authenticator;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// other imports
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OutboundMailService {
    private static final Logger log = LoggerFactory.getLogger(OutboundMailService.class);
    private final MailAuthService authService;
    private final WebClient webClient = WebClient.create();
    private final SimpleEncryptor encryptor;
    private final ObjectMapper mapper = new ObjectMapper();
    private final TicketMessageRepository ticketMessageRepository;


//    public void sendViaGraph(
//            MailIntegration integration,
//            Ticket ticket,
//            List<String> toEmails,
//            List<String> ccEmails,
//            String subject,
//            String htmlBody) {
//
//        try {
//            if (toEmails == null || toEmails.isEmpty()) {
//                throw new IllegalArgumentException("To recipient list must not be empty");
//            }
//
//            String token = authService.getDecryptedAccessToken(integration);
//            if (token == null || token.isBlank()) {
//                throw new IllegalStateException("Access token is null or empty");
//            }
//
//            // ✅ Build recipients (must be List<Map<String,Object>>)
//            List<Map<String, Object>> toRecipients = toEmails.stream()
//                    .filter(addr -> addr != null && !addr.isBlank())
//                    .map(addr -> {
//                        Map<String, Object> emailAddress = new HashMap<>();
//                        emailAddress.put("address", addr.trim());
//                        Map<String, Object> recipient = new HashMap<>();
//                        recipient.put("emailAddress", emailAddress);
//                        return recipient;
//                    })
//                    .collect(Collectors.toList());
//
//            List<Map<String, Object>> ccRecipients = null;
//            if (ccEmails != null && !ccEmails.isEmpty()) {
//                ccRecipients = ccEmails.stream()
//                        .filter(addr -> addr != null && !addr.isBlank())
//                        .map(addr -> {
//                            Map<String, Object> emailAddress = new HashMap<>();
//                            emailAddress.put("address", addr.trim());
//                            Map<String, Object> recipient = new HashMap<>();
//                            recipient.put("emailAddress", emailAddress);
//                            return recipient;
//                        })
//                        .collect(Collectors.toList());
//            }
//
//            // ✅ Build message payload
//            Map<String, Object> body = new HashMap<>();
//            body.put("contentType", "HTML");
//            body.put("content", htmlBody);
//
//            Map<String, Object> message = new LinkedHashMap<>();
//            message.put("subject", subject);
//            message.put("body", body);
//            message.put("toRecipients", toRecipients);
//            if (ccRecipients != null && !ccRecipients.isEmpty()) {
//                message.put("ccRecipients", ccRecipients);
//            }
//
//            // Optional threading support
//
//        if (ticket.getInReplyTo() != null) {
//            List<Map<String, Object>> headers = List.of(
//                    Map.of("name", "In-Reply-To", "value", ticket.getInReplyTo()),
//                    Map.of("name", "References", "value", ticket.getInReplyTo())
//            );
//            message.put("internetMessageHeaders", headers);
//        }
//
//
//            Map<String, Object> payload = new LinkedHashMap<>();
//            payload.put("message", message);
//            payload.put("saveToSentItems", true);
//
//            // ✅ Create draft
//            Map<?, ?> draft = webClient.post()
//                    .uri("https://graph.microsoft.com/v1.0/me/messages")
//                    .headers(h -> h.setBearerAuth(token))
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .bodyValue(message)
//                    .retrieve()
//                    .onStatus(status -> !status.is2xxSuccessful(), response ->
//                            response.bodyToMono(String.class).flatMap(errorBody -> {
//                                log.error("Graph API draft creation error: {}", errorBody);
//                                return Mono.error(new RuntimeException("Graph draft creation error: " + errorBody));
//                            })
//                    )
//                    .bodyToMono(Map.class)
//                    .block();
//
//            if (draft == null || !draft.containsKey("id")) {
//                throw new IllegalStateException("Draft creation failed, no ID returned");
//            }
//
//            String messageId = (String) draft.get("id");
//            String internetMessageId = (String) draft.get("internetMessageId");
//
//            log.info("✅ Draft created via Graph: messageId={}, internetMessageId={}", messageId, internetMessageId);
//
//            // ✅ Send the draft
//            webClient.post()
//                    .uri("https://graph.microsoft.com/v1.0/me/messages/" + messageId + "/send")
//                    .headers(h -> h.setBearerAuth(token))
//                    .retrieve()
//                    .toBodilessEntity()
//                    .block();
//
//            log.info("📨 Mail sent successfully for ticket {}", ticket.getPublicId());
//
//            // Optionally store threading info
//        /*
//        if (internetMessageId != null) {
//            ticket.setEmailMessageId(internetMessageId);
//            ticket.setInReplyTo(internetMessageId);
//        }
//        */
//
//        } catch (Exception e) {
//            log.error("❌ Failed to send mail via Graph API for ticket {}: {}",
//                    ticket.getPublicId(), e.getMessage(), e);
//        }
//    }


    public void sendViaGraph(
            MailIntegration integration,
            Ticket ticket,
            List<String> toEmails,
            List<String> ccEmails,
            String subject,
            String htmlBody) {


        try {

            // Step 1: Find last ticket message
            TicketMessage lastMsg = ticketMessageRepository.findTopByTicketOrderByCreatedAtDesc(ticket)
                    .orElse(null);

            String lastMessageId = (lastMsg != null) ? lastMsg.getEmailMessageId() : null;

            if (toEmails == null || toEmails.isEmpty()) {
                throw new IllegalArgumentException("To recipient list must not be empty");
            }

            String token = authService.getDecryptedAccessToken(integration);
            if (token == null || token.isBlank()) {
                throw new IllegalStateException("Access token is null or empty");
            }

            List<Map<String, Object>> toRecipients = toEmails.stream()
                    .filter(addr -> addr != null && !addr.isBlank())
                    .map(addr -> {
                        Map<String, Object> emailAddress = new HashMap<>();
                        emailAddress.put("address", addr.trim());
                        Map<String, Object> recipient = new HashMap<>();
                        recipient.put("emailAddress", emailAddress);
                        return recipient;
                    })
                    .collect(Collectors.toList());

            List<Map<String, Object>> ccRecipients = null;
            if (ccEmails != null && !ccEmails.isEmpty()) {
                ccRecipients = ccEmails.stream()
                        .filter(addr -> addr != null && !addr.isBlank())
                        .map(addr -> {
                            Map<String, Object> emailAddress = new HashMap<>();
                            emailAddress.put("address", addr.trim());
                            Map<String, Object> recipient = new HashMap<>();
                            recipient.put("emailAddress", emailAddress);
                            return recipient;
                        })
                        .collect(Collectors.toList());
            }

            Map<String, Object> body = Map.of(
                    "contentType", "HTML",
                    "content", htmlBody
            );

            Map<String, Object> message = new LinkedHashMap<>();
            message.put("subject", subject);
            message.put("body", body);
            message.put("toRecipients", toRecipients);

            if (ccRecipients != null && !ccRecipients.isEmpty()) {
                message.put("ccRecipients", ccRecipients);
            }

            // THREADING HEADERS: Include In-Reply-To & References headers when replying
//            if (lastMessageId != null && !lastMessageId.isBlank()) {
//                // Use lastMsg.getEmailMessageId(), i.e. the original message internetMessageId
//                List<Map<String, String>> headers = List.of(
//                        Map.of("name", "In-Reply-To", "value", lastMessageId),
//                        Map.of("name", "References", "value", lastMessageId)
//                );
//                message.put("internetMessageHeaders", headers);
//            }

            Map<String, Object> payload = Map.of(
                    "message", message,
                    "saveToSentItems", true
            );

            // Create draft message
            Map<?, ?> draft = webClient.post()
                    .uri("https://graph.microsoft.com/v1.0/me/messages")
                    .headers(h -> h.setBearerAuth(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(message)
                    .retrieve()
                    .onStatus(status -> !status.is2xxSuccessful(), response ->
                            response.bodyToMono(String.class).flatMap(errorBody -> {
                                log.error("Graph API draft creation error: {}", errorBody);
                                return Mono.error(new RuntimeException("Graph draft creation error: " + errorBody));
                            })
                    )
                    .bodyToMono(Map.class)
                    .block();

            if (draft == null || !draft.containsKey("id")) {
                throw new IllegalStateException("Draft creation failed, no ID returned");
            }

            String messageId = (String) draft.get("id");
            String internetMessageId = (String) draft.get("internetMessageId");

//            log.info("Draft created: messageId={}, internetMessageId={}", messageId, internetMessageId);

            // Send the draft
            webClient.post()
                    .uri("https://graph.microsoft.com/v1.0/me/messages/" + messageId + "/send")
                    .headers(h -> h.setBearerAuth(token))
                    .retrieve()
                    .toBodilessEntity()
                    .block();

//            log.info("Mail sent successfully for ticket {}", ticket.getPublicId());

            // Store internetMessageId for threading in the ticket entity or ticketMessage entity
//            if (internetMessageId != null) {
//                ticket.setEmailMessageId(internetMessageId);
//                ticket.setInReplyTo(internetMessageId);
//                // Persist ticket or ticketMessage with updated messageId for next threading mail
//                // example: ticketRepository.save(ticket);
//            }

//            assert lastMsg != null;
//            lastMsg.setEmailMessageId(messageId);
//            lastMsg.setInReplyTo(lastMessageId);
//            ticketMessageRepository.save(lastMsg);

        } catch (Exception e) {
            log.error("Failed to send mail via Graph API for ticket {}: {}", ticket.getPublicId(), e.getMessage(), e);
        }
    }


    private String extractHeaderGraph(List<Map<String, Object>> headers, String name) {
            if (headers == null || headers.isEmpty())
                return null;
            for (Map<String, Object> header : headers) {
                Object headerNameObj = header.get("name");
                Object headerValueObj = header.get("value");
                String headerName = (headerNameObj != null) ? headerNameObj.toString() : null;
                String headerValue = (headerValueObj != null) ? headerValueObj.toString() : null;
                if (headerName != null && headerName.equalsIgnoreCase(name)) {
                    return headerValue;
                }
            }
            return null;
        }

    public void sendGmailMessage(MailIntegration integration,
                                 Ticket ticket,
                                 List<String> toEmails,
                                 List<String> ccEmails,
                                 String subject,
                                 String htmlBody) {

        String accessToken = authService.getDecryptedAccessToken(integration);

        // Step 1: Find last ticket message
        TicketMessage lastMsg = ticketMessageRepository.findTopByTicketOrderByCreatedAtDesc(ticket)
                .orElse(null);

        String lastMessageId = (lastMsg != null) ? lastMsg.getEmailMessageId() : null;

        // Step 2: Build RFC822 raw email
        StringBuilder rawMessage = new StringBuilder();
        rawMessage.append("From: ").append(integration.getMailboxEmail()).append("\r\n");
        rawMessage.append("To: ").append(String.join(",", toEmails)).append("\r\n");
        if (ccEmails != null && !ccEmails.isEmpty()) {
            rawMessage.append("Cc: ").append(String.join(",", ccEmails)).append("\r\n");
        }
        rawMessage.append("Subject: ").append(subject).append("\r\n");
        rawMessage.append("Content-Type: text/html; charset=UTF-8\r\n");

        // Step 3: Threading headers if reply
        if (lastMessageId != null) {
            rawMessage.append("In-Reply-To: ").append(lastMessageId).append("\r\n");
            rawMessage.append("References: ").append(lastMessageId).append("\r\n");
        }

        rawMessage.append("\r\n"); // separate headers and body
        rawMessage.append(htmlBody);

        // Step 4: Gmail expects base64url encoding
        String base64UrlEncoded = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(rawMessage.toString().getBytes(StandardCharsets.UTF_8));

        Map<String, String> payload = Map.of("raw", base64UrlEncoded);

        // Step 5: Send
        Map response = webClient.post()
                .uri("https://gmail.googleapis.com/gmail/v1/users/me/messages/send")
                .headers(h -> h.setBearerAuth(accessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        log.info("Gmail send response: {}", response);

        // Step 6: Store new message in DB
        // Gmail response has "id" (internalId) but not always the Internet Message-ID
        // To get the real Message-ID header, you must fetch it again with format=full
        String gmailMessageId = (String) response.get("id");

        Map sentMsg = webClient.get()
                .uri("https://gmail.googleapis.com/gmail/v1/users/me/messages/{id}?format=full", gmailMessageId)
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        String newMessageId = extractHeader((List<Map>) ((Map) sentMsg.get("payload")).get("headers"), "Message-ID");

        assert lastMsg != null;
        lastMsg.setEmailMessageId(newMessageId);
        lastMsg.setInReplyTo(lastMessageId);
        ticketMessageRepository.save(lastMsg);
    }

    private String extractHeader(List<Map> headers, String name) {
        for (Map header : headers) {
            if (name.equalsIgnoreCase((String) header.get("name"))) {
                return (String) header.get("value");
            }
        }
        return null;
    }


}

