package com.InfraDesk.service;

import com.InfraDesk.dto.CreateTicketRequest;
import com.InfraDesk.dto.TicketMessageRequest;
import com.InfraDesk.entity.*;
import com.InfraDesk.enums.TicketPriority;
import com.InfraDesk.enums.TicketStatus;
import com.InfraDesk.exception.BusinessException;
import com.InfraDesk.repository.*;
import com.InfraDesk.util.TicketMessageHelper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class MailProcessorService {

    private static final Logger log = LoggerFactory.getLogger(MailProcessorService.class);
    private final TicketRepository ticketRepo;
    private final TicketMessageRepository messageRepo;
    private final CompanyRepository companyRepository;
    private final TicketService ticketService;
    private final TicketMessageService ticketMessageService;
    private final TicketingDepartmentConfigRepository ticketingDepartmentConfigRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final TicketMessageHelper ticketMessageHelper; // your helper to add message easily

    // For Gmail message (JSON)
    public void processGmailMessage(Map fullMessage, MailIntegration integration) {
        Map payload = (Map) fullMessage.get("payload");
        List<Map> headers = (List<Map>) payload.get("headers");
        String subject = findHeader(headers, "Subject");
        String from = findHeader(headers, "From");
        String messageId = findHeader(headers, "Message-ID");
        String inReplyTo = findHeader(headers, "In-Reply-To");
        String rawBody = extractPlainTextFromGmailPayload(payload); // implement recursive
        String body = cleanEmailBody(rawBody);
        handleIncomingMail(integration, subject, from, messageId, inReplyTo, body);
    }

    public void processGraphMessage(Map<String, Object> fullMessage, MailIntegration integration) {
        try {
            // Extract common properties from Graph message
            String subject = (String) fullMessage.get("subject");
            String from = null;
            String messageId = (String) fullMessage.get("internetMessageId");
            String inReplyTo = (String) fullMessage.get("inReplyTo");

            // Extract sender info
            Map<String, Object> fromMap = (Map<String, Object>) fullMessage.get("from");
            if (fromMap != null) {
                Map<String, Object> emailAddress = (Map<String, Object>) fromMap.get("emailAddress");
                if (emailAddress != null) {
                    String senderName = (String) emailAddress.get("name");
                    String senderAddress = (String) emailAddress.get("address");
                    from = senderName != null ? String.format("%s <%s>", senderName, senderAddress) : senderAddress;
                }
            }

            // Extract body content
            Map<String, Object> body = (Map<String, Object>) fullMessage.get("body");
            String bodyContent = "";
            if (body != null) {
                String contentType = (String) body.get("contentType");
                String content = (String) body.get("content");
                if ("text".equalsIgnoreCase(contentType)) {
                    bodyContent = content;
                } else if ("html".equalsIgnoreCase(contentType)) {
                    // Convert HTML to plain text, preserving line breaks
                    bodyContent = htmlToPlainTextPreserveBreaks(content);
                }
            }

            // Clean email body to remove signatures, quoted text, etc.
            String cleanedBody = cleanMicrosoftEmailBody(bodyContent);

            // Call your existing mail handler method
            handleIncomingMail(integration, subject, from, messageId, inReplyTo, cleanedBody);

        } catch (Exception e) {
            log.error("Error processing Outlook message: {}", fullMessage, e);
        }
    }

    /**
     * Converts HTML content to plain text, preserving line breaks from <br> and <p>.
     */
    private String htmlToPlainTextPreserveBreaks(String html) {
        if (html == null || html.isEmpty()) return "";

        Document doc = Jsoup.parse(html);

        // Insert newlines at <br> tags
        doc.select("br").forEach(br -> br.after("\\n"));
        // Insert newlines before and after <p>
        doc.select("p").forEach(p -> {
            p.prepend("\\n");
            p.append("\\n");
        });

        String textWithMarkers = doc.text();

        // Replace markers with actual line breaks
        return textWithMarkers.replace("\\n", "\n");
    }

    /**
     * Cleans the email body by removing signatures, quoted replies, and forwarded content.
     */
    private String cleanMicrosoftEmailBody(String rawBody) {
        if (rawBody == null) return "";

        String text = rawBody.strip();

        String[] markers = {
                "(?i)On .*wrote:",
                "(?i)^From:.*",
                "(?i)^Sent:.*",
                "(?i)^To:.*",
                "(?i)^Subject:.*",
                "-----Original Message-----",
                "----- Forwarded Message -----",
                "________________________________",
                "sent from",
                "confidentiality notice"
        };

        for (String marker : markers) {
            Pattern p = Pattern.compile(marker, Pattern.MULTILINE);
            Matcher m = p.matcher(text);
            if (m.find()) {
                // Cut everything before the marker
                text = text.substring(0, m.start());
                break;
            }
        }
        return text;
    }

    private void handleIncomingMail(
            MailIntegration integration,
            String subject,
            String from,
            String messageId,
            String inReplyTo,
            String body
            // Pass this in if you extract files; see note
    ) {

        // 1. Deduplicate by messageId
        if (messageRepo.findByEmailMessageIdWithTicket(messageId).isPresent()) {
            log.info("Already processed Message-ID: {}", messageId);
            return;
        }

        // 2. Check message thread by inReplyTo
        if (inReplyTo != null) {
            Optional<TicketMessage> parent = messageRepo.findByEmailMessageIdWithTicket(inReplyTo);
            if (parent.isPresent()) {
                Ticket ticket = parent.get().getTicket();
                appendMessageToTicket(ticket, body, from, integration,messageId,inReplyTo);
                return;
            }
        }

        // 3. Parse ticket reference from subject
        String ticketPublicId = parseTicketIdFromSubject(subject);
        if (ticketPublicId != null) {
            ticketRepo.findByPublicIdAndCompany_PublicId(ticketPublicId, integration.getCompanyId())
                    .ifPresent(ticket -> {
                        appendMessageToTicket(ticket, body, from, integration,messageId,inReplyTo);
                    });
            return;
        }

        // 4. Department lookup
        Company company = companyRepository.findByPublicId(integration.getCompanyId())
                .orElseThrow(() -> new BusinessException("Company not found: " + integration.getCompanyId()));

        TicketingDepartmentConfig deptConfig = ticketingDepartmentConfigRepository
                .findWithDepartmentByTicketEmail(integration.getMailboxEmail())
                .orElseThrow(() -> new BusinessException("Active Ticketing department not found for " + integration.getMailboxEmail()));

        // 5. Validate sender domain
        if (!deptConfig.getAllowTicketsFromAnyDomain()) {
            String senderDomain = extractEmailDomain(from).trim().toLowerCase();
            boolean allowed = deptConfig.getAllowedTicketDomains()
                    .stream()
                    .map(String::toLowerCase)
                    .map(String::trim) // trim here as well
                    .anyMatch(domain -> domain.equals(senderDomain));

            if (!allowed) {
                throw new BusinessException("Email domain not allowed: " + senderDomain);
            }
        }
        String departmentPublicId=deptConfig.getDepartment().getPublicId();

        // 6. Look up default ticket type
        TicketType defaultType = ticketTypeRepository
                .findByCompanyPublicIdAndNameContainingIgnoreCaseAndActiveTrue(company.getPublicId(), "OTHER")
                .orElseThrow(() -> new BusinessException("Default ticket type OTHER not found"));

        // 7. Build DTO for new ticket
        CreateTicketRequest ticketReq = CreateTicketRequest.builder()
                .subject(subject != null ? subject : "(no subject)")
                .creatorEmail(extractEmailAddress(from))
                .departmentId(departmentPublicId)
                .status(TicketStatus.OPEN)
                .priority(TicketPriority.MEDIUM)
                .ticketTypeId(defaultType.getPublicId())
                .description(body)
                .emailMessageId(messageId)
                .inReplyTo(inReplyTo)
                .attachments(null)
                .build();

        try {
            ticketService.createTicket(ticketReq,company.getPublicId());
        }catch (Exception e){
            log.error("Exception found to create the ticket in mail service ",e);
        }

    }

    private String extractEmailAddress(String from) {
        if (from == null) return null;
        from = from.trim();
        // Check for the pattern: Name <email@example.com>
        int start = from.indexOf('<');
        int end = from.indexOf('>');
        if (start >= 0 && end > start) {
            // Extract substring between <>
            return from.substring(start + 1, end).trim();
        }
        // If no <>, assume the whole string is email
        log.info("Returning email is {}",from);
        return from;
    }

    // Utility method to extract domain (for sender validation)
//    private String extractEmailDomain(String email) {
//        int atIdx = email.trim().lastIndexOf("@");
//        if (atIdx < 0 || atIdx == email.length() - 1) {
//            throw new IllegalArgumentException("Invalid sender email: " + email);
//        }
//        return email.substring(atIdx + 1).toLowerCase();
//    }

    private String extractEmailDomain(String from) {
        if (from == null) throw new IllegalArgumentException("Sender email is null");
        // Extract email address part
        String email;
        int start = from.indexOf('<');
        int end = from.indexOf('>');
        if (start >= 0 && end > start) {
            email = from.substring(start + 1, end);
        } else {
            email = from;
        }
        email = email.trim();
        int atIdx = email.lastIndexOf('@');
        if (atIdx < 0 || atIdx == email.length() - 1) {
            throw new IllegalArgumentException("Invalid sender email: " + from);
        }
        return email.substring(atIdx + 1).toLowerCase().trim();
    }


    private void appendMessageToTicket(
            Ticket ticket, String body, String from
            , MailIntegration integration,String emailMessageId
            , String inReplyTo) {

        TicketMessageRequest req = TicketMessageRequest.builder()
                .ticketId(ticket.getPublicId())
                .senderEmail(extractEmailAddress(from))
                .body(body)
                .internalNote(false)
                .emailMessageId(emailMessageId)
                .inReplyTo(inReplyTo)
                .attachments(null)
                .build();
        ticketMessageHelper.addMessage(req, integration.getCompanyId().toString()); // adapt param types

    }

    private String findHeader(List<Map> headers, String name) {
        for (Map header : headers) {
            if (name.equalsIgnoreCase((String) header.get("name"))) {
                return (String) header.get("value");
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public String extractPlainTextFromGmailPayload(Map<String, Object> payload) {
        if (payload == null) return "";

        String mimeType = (String) payload.get("mimeType");
        Map<String, Object> body = (Map<String, Object>) payload.get("body");

        if ("text/plain".equalsIgnoreCase(mimeType) && body != null) {
            String data = (String) body.get("data");
            if (data != null && !data.isEmpty()) {
                byte[] decodedBytes = java.util.Base64.getUrlDecoder().decode(data);
                return new String(decodedBytes, java.nio.charset.StandardCharsets.UTF_8);
            }
        }

        List<Map<String, Object>> parts = (List<Map<String, Object>>) payload.get("parts");
        if (parts != null) {
            for (Map<String, Object> part : parts) {
                String text = extractPlainTextFromGmailPayload(part);
                if (text != null && !text.isEmpty()) {
                    return text;
                }
            }
        }

        return "";
    }

    private String cleanEmailBody(String rawBody) {
        if (rawBody == null) return "";

        String[] lines = rawBody.split("\\r?\\n");
        StringBuilder clean = new StringBuilder();

        for (String line : lines) {
            String trimmed = line.trim();

            // Stop at common previous-message or signature markers
            if (trimmed.matches("^On .*wrote:.*$") ||
                    trimmed.startsWith(">") ||
                    trimmed.startsWith("-- ") ||
                    trimmed.startsWith("From:") ||
                    trimmed.startsWith("Sent:") ||
                    trimmed.startsWith("To:") ||
                    trimmed.startsWith("Subject:") ||
                    trimmed.startsWith("-----Original Message-----") ||
                    trimmed.startsWith("______________________________")) {
                break;
            }

            clean.append(line).append("\n"); // preserve original line breaks
        }

        // Remove trailing empty lines
        while (clean.length() > 0 && clean.toString().endsWith("\n")) {
            clean.setLength(clean.length() - 1);
        }

        return clean.toString();
    }

    private String parseTicketIdFromSubject(String subject) {
        if (subject == null) return null;

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\[([A-Z]{2,}-[0-9]+)\\]");
        java.util.regex.Matcher matcher = pattern.matcher(subject);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}


