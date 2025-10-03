package com.InfraDesk.service;

import com.InfraDesk.component.SimpleEncryptor;
import com.InfraDesk.entity.MailIntegration;
import com.InfraDesk.entity.Ticket;
import com.InfraDesk.entity.TicketMessage;
import com.InfraDesk.repository.TicketMessageRepository;
import jakarta.mail.Authenticator;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Service
@RequiredArgsConstructor
public class OutboundMailService {
    private static final Logger log = LoggerFactory.getLogger(OutboundMailService.class);
    private final MailAuthService authService;
    private final WebClient webClient = WebClient.create();
    private final SimpleEncryptor encryptor;
    private final TicketMessageRepository ticketMessageRepository;

    public void sendViaGraph(MailIntegration integration, String to, String subject, String htmlBody) {
        String token = authService.getDecryptedAccessToken(integration);
        Map message = Map.of("message", Map.of(
                "subject", subject,
                "body", Map.of("contentType", "HTML", "content", htmlBody),
                "toRecipients", List.of(Map.of("emailAddress", Map.of("address", to)))
        ));
        webClient.post()
                .uri("https://graph.microsoft.com/v1.0/me/sendMail")
                .headers(h -> h.setBearerAuth(token))
                .bodyValue(message)
                .retrieve().bodyToMono(Void.class).block();
    }


//    public void sendUsingSmtp(MailIntegration integration, String to, String subject, String htmlBody) throws MessagingException {
//        JavaMailSenderImpl sender = new JavaMailSenderImpl();
//        sender.setHost(integration.getSmtpHost());
//        sender.setPort(integration.getSmtpPort());
//        sender.setUsername(integration.getMailboxEmail());
//        sender.setPassword(encryptor.decrypt(integration.getEncryptedSmtpPassword()));
//
//        Properties props = sender.getJavaMailProperties();
//        props.put("mail.transport.protocol", "smtp");
//        props.put("mail.smtp.auth", String.valueOf(true));
//        props.put("mail.smtp.starttls.enable", String.valueOf(integration.getSmtpTls() != null && integration.getSmtpTls()));
//        props.put("mail.debug", "false");
//
//        MimeMessage message = sender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//        helper.setFrom(integration.getMailboxEmail());
//        helper.setTo(to);
//        helper.setSubject(subject);
//        helper.setText(htmlBody, true);
//        sender.send(message);
//    }

//        public void sendMailForGmail(
//                MailIntegration integration,
//                List<String> toEmails,
//                List<String> ccEmails,
//                String subject,
//                String htmlBody
//        ) throws MessagingException {
//            if (toEmails == null || toEmails.isEmpty()) {
//                throw new IllegalArgumentException("At least one recipient required");
//            }
//
//            log.info("Sending email to {} with subject '{}'", toEmails.get(0), subject);
//
//            String oauth2AccessToken = authService.getDecryptedAccessToken(integration);
//
//            JavaMailSenderImpl sender = new JavaMailSenderImpl();
////
////            sender.setHost(integration.getSmtpHost());
////            sender.setPort(integration.getSmtpPort());
////            sender.setUsername(integration.getMailboxEmail());
//
//            sender.setHost(integration.getSmtpHost());
//            if (integration.getSmtpPort() != null) {
//                sender.setPort(integration.getSmtpPort());
//            } else {
//                sender.setPort(587); // default SMTP port for TLS
//                log.warn("smtpPort was null, defaulting to 587");
//            }
//            sender.setUsername(integration.getMailboxEmail());
//
//            Properties props = sender.getJavaMailProperties();
//            props.put("mail.transport.protocol", "smtp");
//            props.put("mail.smtp.auth", "true");
//            props.put("mail.smtp.starttls.enable", String.valueOf(Boolean.TRUE.equals(integration.getSmtpTls())));
//            props.put("mail.smtp.ssl.trust", integration.getSmtpHost());
//            props.put("mail.debug", "false");
//
//            if (oauth2AccessToken != null && !oauth2AccessToken.isBlank()) {
//                sender.setSession(Session.getInstance(props, new Authenticator() {
//                    @Override
//                    protected PasswordAuthentication getPasswordAuthentication() {
//                        return new PasswordAuthentication(integration.getMailboxEmail(), oauth2AccessToken);
//                    }
//                }));
//            } else {
//                sender.setPassword(encryptor.decrypt(integration.getEncryptedSmtpPassword()));
//            }
//
//            MimeMessage message = sender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//            helper.setFrom(integration.getMailboxEmail());
//            helper.setTo(toEmails.toArray(new String[0]));
//
//            if (ccEmails != null && !ccEmails.isEmpty()) {
//                helper.setCc(ccEmails.toArray(new String[0]));
//            }
//
//            helper.setSubject(subject);
//            helper.setText(htmlBody, true);  // true = HTML content
//
//            sender.send(message);
//
//            log.info("Email sent successfully to {}", toEmails);
//        }
//


//    public void sendMailForGmail(
//            MailIntegration integration,
//            List<String> toEmails,
//            List<String> ccEmails,
//            String subject,
//            String htmlBody
//    ) throws MessagingException {
//
//        if (toEmails == null || toEmails.isEmpty()) {
//            throw new IllegalArgumentException("At least one recipient required");
//        }
//
//        String oauth2AccessToken = authService.getDecryptedAccessToken(integration);
//
//        JavaMailSenderImpl sender = new JavaMailSenderImpl();
//
//        String smtpHost = integration.getSmtpHost();
//        Integer smtpPort = integration.getSmtpPort();
//
//        // Determine SMTP Host and Port based on provider if missing
//        if (smtpHost == null || smtpHost.isBlank()) {
//            switch (integration.getProvider().toUpperCase()) {
//                case "GMAIL":
//                    smtpHost = "smtp.gmail.com";
//                    smtpPort = 587; // TLS port
//                    break;
//                case "MICROSOFT":
//                    smtpHost = "smtp.office365.com";
//                    smtpPort = 587; // TLS port
//                    break;
//                default:
//                    throw new IllegalStateException("SMTP Host not configured and unknown provider: " + integration.getProvider());
//            }
//        }
//
//        sender.setHost(smtpHost);
//        sender.setPort(smtpPort != null ? smtpPort : 587);
//        sender.setUsername(integration.getMailboxEmail());
//
//        Properties props = sender.getJavaMailProperties();
//        props.put("mail.transport.protocol", "smtp");
//        props.put("mail.smtp.auth", "true");
//        props.put("mail.smtp.starttls.enable", "true");
//        props.put("mail.smtp.starttls.required", "true");
//        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
//        props.put("mail.smtp.ssl.trust", smtpHost);
//        props.put("mail.debug", "true");
//
//
//        if (oauth2AccessToken != null && !oauth2AccessToken.isBlank()) {
//            sender.setSession(Session.getInstance(props, new Authenticator() {
//                @Override
//                protected PasswordAuthentication getPasswordAuthentication() {
//                    // Use OAuth2 access token as password
//                    return new PasswordAuthentication(integration.getMailboxEmail(), oauth2AccessToken);
//                }
//            }));
//            log.info("Using OAuth2 authentication for SMTP host {} on port {}", smtpHost, sender.getPort());
//        } else {
//            // Fallback to SMTP username/password auth
//            String decryptedPassword = encryptor.decrypt(integration.getEncryptedSmtpPassword());
//            sender.setPassword(decryptedPassword);
//            log.info("Using SMTP password authentication for SMTP host {} on port {}", smtpHost, sender.getPort());
//        }
//
//        log.info("Preparing email to: {} cc: {} with subject: {}", toEmails, ccEmails, subject);
//
//        MimeMessage message = sender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//        helper.setFrom(integration.getMailboxEmail());
//        helper.setTo(toEmails.toArray(new String[0]));
//
//        if (ccEmails != null && !ccEmails.isEmpty()) {
//            helper.setCc(ccEmails.toArray(new String[0]));
//        }
//
//        helper.setSubject(subject);
//        helper.setText(htmlBody, true);  // true = HTML email content
//
//        sender.send(message);
//
//        log.info("Email sent successfully to {}", toEmails);
//    }

//    public void sendGmailMessage(MailIntegration integration,
//                                 List<String> toEmails,
//                                 List<String> ccEmails,
//                                 String subject,
//                                 String htmlBody) {
//
//        String accessToken = authService.getDecryptedAccessToken(integration);
//
//        // Build raw RFC822 message
//        StringBuilder rawMessage = new StringBuilder();
//        rawMessage.append("From: ").append(integration.getMailboxEmail()).append("\r\n");
//        rawMessage.append("To: ").append(String.join(",", toEmails)).append("\r\n");
//        if (ccEmails != null && !ccEmails.isEmpty()) {
//            rawMessage.append("Cc: ").append(String.join(",", ccEmails)).append("\r\n");
//        }
//        rawMessage.append("Subject: ").append(subject).append("\r\n");
//        rawMessage.append("Content-Type: text/html; charset=UTF-8\r\n\r\n");
//        rawMessage.append(htmlBody);
//
//        // Gmail expects base64url encoding
//        String base64UrlEncoded = Base64.getUrlEncoder()
//                .withoutPadding()
//                .encodeToString(rawMessage.toString().getBytes(StandardCharsets.UTF_8));
//
//        Map<String, String> payload = Map.of("raw", base64UrlEncoded);
//
//        Map response = webClient.post()
//                .uri("https://gmail.googleapis.com/gmail/v1/users/me/messages/send")
//                .headers(h -> h.setBearerAuth(accessToken))
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(payload)
//                .retrieve()
//                .bodyToMono(Map.class)
//                .block();
//
//        log.info("Gmail send response: {}", response);
//    }

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

