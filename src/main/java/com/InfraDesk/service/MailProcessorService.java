package com.InfraDesk.service;

import com.InfraDesk.dto.TicketMessageRequest;
import com.InfraDesk.entity.MailIntegration;
import com.InfraDesk.entity.Ticket;
import com.InfraDesk.entity.TicketMessage;
import com.InfraDesk.repository.TicketMessageRepository;
import com.InfraDesk.repository.TicketRepository;
import com.InfraDesk.repository.UserRepository;
import com.InfraDesk.util.TicketMessageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MailProcessorService {

    private final TicketRepository ticketRepo;
    private final TicketMessageRepository messageRepo;
    private final UserRepository userRepo;
    private final TicketMessageHelper ticketMessageHelper; // your helper to add message easily

    // For Gmail message (JSON)
    public void processGmailMessage(Map fullMessage, MailIntegration integration) {
        Map payload = (Map) fullMessage.get("payload");
        List<Map> headers = (List<Map>) payload.get("headers");
        String subject = findHeader(headers, "Subject");
        String from = findHeader(headers, "From");
        String messageId = findHeader(headers, "Message-ID");
        String inReplyTo = findHeader(headers, "In-Reply-To");
        String body = extractPlainTextFromGmailPayload(payload); // implement recursive

        handleIncomingMail(integration, subject, from, messageId, inReplyTo, body);
    }

    public void processGraphMessage(Map msg, MailIntegration integration) {
        String subject = (String) msg.get("subject");
        Map from = (Map) ((Map) msg.get("from")).get("emailAddress");
        String fromEmail = (String) from.get("address");
        String messageId = (String) msg.get("internetMessageId");
        String inReplyTo = (String) msg.get("inReplyTo");
        String body = ((Map) msg.get("body")).get("content").toString();
        handleIncomingMail(integration, subject, fromEmail, messageId, inReplyTo, body);
    }

    private void handleIncomingMail(MailIntegration integration, String subject, String from, String messageId, String inReplyTo, String body) {
        // 1. Try to find existing message by inReplyTo or messageId (dedup)
        Optional<TicketMessage> existing = messageRepo.findByEmailMessageId(messageId);
        if (existing.isPresent()) return; // already processed

        // 2. Try to find ticket by In-Reply-To -> ticket_message.email_message_id
        if (inReplyTo != null) {
            Optional<TicketMessage> parent = messageRepo.findByEmailMessageId(inReplyTo);
            if (parent.isPresent()) {
                Ticket ticket = parent.get().getTicket();
                appendMessageToTicket(ticket, body, from, integration);
                return;
            }
        }

        // 3. Try to parse subject for ticket token e.g. [T-ABC123]
        String ticketPublicId = parseTicketIdFromSubject(subject);
        if (ticketPublicId != null) {
            ticketRepo.findByPublicIdAndCompanyId(ticketPublicId, integration.getCompanyId())
                    .ifPresent(ticket -> {
                        appendMessageToTicket(ticket, body, from, integration);
                    });
            return;
        }

        // 4. Otherwise create new ticket (subject/body mapped)
        Ticket t = new Ticket();
        t.setPublicId(generateTicketPublicId()); // implement
        t.setCompanyId(integration.getCompanyId());
        t.setSubject(subject != null ? subject : "(no subject)");
        t.setDescription(body);
        // set default status/priority/assignee as per company rules
        ticketRepo.save(t);
        appendMessageToTicket(t, body, from, integration);
    }

    private void appendMessageToTicket(Ticket ticket, String body, String from, MailIntegration integration) {
        TicketMessageRequest req = TicketMessageRequest.builder()
                .ticketId(ticket.getPublicId())
                .senderEmail(from)
                .body(body)
                .internalNote(false)
                .build();
        ticketMessageHelper.addMessage(req, integration.getCompanyId().toString()); // adapt param types
    }

    // helper methods findHeader(), extractPlainTextFromGmailPayload(), parseTicketIdFromSubject() — implement them robustly
}

