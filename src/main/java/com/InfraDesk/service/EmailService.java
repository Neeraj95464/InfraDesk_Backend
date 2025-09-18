package com.InfraDesk.service;

import com.InfraDesk.entity.Ticket;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

import java.util.List;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final String fromAddress = "support@yourdomain.com"; // make configurable

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendTicketCreated(Ticket ticket, List<String> recipients) {
        try {
            MimeMessage m = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(m, true);
            helper.setFrom(fromAddress);
            helper.setTo(recipients.toArray(new String[0]));
            // Subject should be ticket title
            helper.setSubject(ticket.getSubject());
            // Build body with link to ticket (frontend)
            String body = "Ticket " + ticket.getPublicId() + " created.\n\n" + ticket.getDescription();
            helper.setText(body, false);
            // set Message-ID for threading - mail libraries auto generate; can set headers too
            mailSender.send(m);
        } catch (Exception e) {
            // log
        }
    }
}

