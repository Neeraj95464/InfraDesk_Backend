package com.InfraDesk.service;

import com.InfraDesk.component.SimpleEncryptor;
import com.InfraDesk.entity.MailIntegration;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.Properties;

@Service
@RequiredArgsConstructor
public class OutboundMailService {
    private final MailAuthService authService;
    private final WebClient webClient = WebClient.create();
    private final SimpleEncryptor encryptor;

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


    public void sendUsingSmtp(MailIntegration integration, String to, String subject, String htmlBody) throws MessagingException {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(integration.getSmtpHost());
        sender.setPort(integration.getSmtpPort());
        sender.setUsername(integration.getMailboxEmail());
        sender.setPassword(encryptor.decrypt(integration.getEncryptedSmtpPassword()));

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", String.valueOf(true));
        props.put("mail.smtp.starttls.enable", String.valueOf(integration.getSmtpTls() != null && integration.getSmtpTls()));
        props.put("mail.debug", "false");

        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(integration.getMailboxEmail());
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        sender.send(message);
    }

}

