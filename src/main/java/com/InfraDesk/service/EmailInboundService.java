package com.InfraDesk.service;

import com.InfraDesk.entity.*;
import com.InfraDesk.enums.TicketPriority;
import com.InfraDesk.enums.TicketStatus;
import com.InfraDesk.repository.*;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

@Service
public class EmailInboundService {

    private final MailboxRepository mailboxRepository;
    private final TicketRepository ticketRepository;
    private final TicketMessageRepository messageRepository;
    private final TicketAttachmentRepository attachmentRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final TicketNumberService ticketNumberService;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final TicketFileStorageService ticketFileStorageService; // ✅ use local storage

    private final Session imapSession;
    private final String imapUser;
    private final String imapPassword;
    private final String imapHost;

    public EmailInboundService(MailboxRepository mailboxRepository,
                               TicketRepository ticketRepository,
                               TicketMessageRepository messageRepository,
                               TicketAttachmentRepository attachmentRepository,
                               TicketFileStorageService ticketFileStorageService,
                               TicketTypeRepository ticketTypeRepository,
                               TicketNumberService ticketNumberService,
                               CompanyRepository companyRepository,
                               UserRepository userRepository
            /* inject IMAP properties or Session */) {
        this.mailboxRepository = mailboxRepository;
        this.ticketRepository = ticketRepository;
        this.messageRepository = messageRepository;
        this.attachmentRepository = attachmentRepository;
        this.ticketFileStorageService = ticketFileStorageService;
        this.ticketTypeRepository = ticketTypeRepository;
        this.ticketNumberService = ticketNumberService;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;

        // For brevity, create session with props here or inject
        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        imapSession = Session.getInstance(props);

        this.imapHost = "imap.yourmail.com";
        this.imapUser = "support@yourdomain.com";
        this.imapPassword = "password";
    }

    @Scheduled(fixedDelayString = "${inbound.poll.ms:30000}")
    public void poll() {
        try (Store store = imapSession.getStore("imaps")) {
            store.connect(imapHost, imapUser, imapPassword);
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            Message[] messages = inbox.getMessages();
            for (Message msg : messages) {
                try {
                    processMessage(msg);
                    msg.setFlag(Flags.Flag.SEEN, true);
                } catch (Exception ex) {
                    // log error but continue
                }
            }
            inbox.close(true);
        } catch (Exception e) {
            // log error
        }
    }

    @Transactional
    public void processMessage(Message msg) throws Exception {
        Mailbox mailbox = null;
        for (Address a : msg.getAllRecipients()) {
            String email = ((InternetAddress) a).getAddress();
            mailbox = mailboxRepository.findByEmailAddressIgnoreCase(email).orElse(null);
            if (mailbox != null) break;
        }

        String subject = msg.getSubject();
        String messageId = msg.getHeader("Message-ID") != null ? msg.getHeader("Message-ID")[0] : null;
        String inReplyTo = msg.getHeader("In-Reply-To") != null ? msg.getHeader("In-Reply-To")[0] : null;

        Ticket ticket = null;

        // try to match existing ticket by subject
        if (subject != null) {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("([A-Z0-9]+\\-[A-Z0-9]+\\-\\d{1,})").matcher(subject);
            if (m.find()) {
                String publicId = m.group(1);
                ticket = ticketRepository.findByPublicId(publicId).orElse(null);
            }
        }

        // try match by in-reply-to
        if (ticket == null && inReplyTo != null) {
            ticket = messageRepository.findAll().stream()
                    .filter(mm -> inReplyTo.equals(mm.getEmailMessageId()))
                    .map(TicketMessage::getTicket)
                    .findFirst()
                    .orElse(null);
        }

        // if still null -> new ticket
        if (ticket == null && mailbox != null) {
            Long companyId = mailbox.getCompany().getId();
            Long deptId = mailbox.getDepartment() == null ? null : mailbox.getDepartment().getId();
            TicketType ticketType = mailbox.getDefaultTicketType() != null ? mailbox.getDefaultTicketType() :
                    ticketTypeRepository.findByCompanyId(companyId).stream().findFirst().orElse(null);

            long seq = ticketNumberService.nextSeq(companyId, deptId);
            String companyCode = mailbox.getCompany().getName()== null ? "C" + companyId : mailbox.getCompany().getName();
            String deptCode = mailbox.getDepartment() == null ? "GEN" : ("D" + mailbox.getDepartment().getId());
            String publicId = String.format("%s-%s-%05d", companyCode, deptCode, seq);

            Ticket t = Ticket.builder()
                    .seq(seq)
                    .publicId(publicId)
                    .company(mailbox.getCompany())
                    .department(mailbox.getDepartment())
                    .ticketType(ticketType)
                    .subject(subject == null ? "(no subject)" : subject)
                    .description("(created from email)")
                    .status(TicketStatus.OPEN)
                    .priority(TicketPriority.MEDIUM)
                    .build();
            ticket = ticketRepository.save(t);
        }

        if (ticket == null) return; // nothing to do

        // parse body text + attachments
        String bodyText = "";
        Object content = msg.getContent();

        if (content instanceof String str) {
            bodyText = str;
        } else if (content instanceof Multipart mp) {
            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart bp = mp.getBodyPart(i);
                if (Part.ATTACHMENT.equalsIgnoreCase(bp.getDisposition())) {
                    String filename = bp.getFileName();
                    try (InputStream is = bp.getInputStream()) {
                        // ✅ use local storage now
                        String storedPath = ticketFileStorageService.storeFile((MultipartFile) bp.getInputStream(),
                                "tickets/" + ticket.getPublicId());

                        TicketAttachment att = TicketAttachment.builder()
                                .ticket(ticket)
                                .originalFileName(filename)
                                .sizeInBytes((long) bp.getSize())
                                .contentType(bp.getContentType())
                                .filePath(storedPath)
                                .build();
                        attachmentRepository.save(att);
                    }
                } else {
                    if (bp.isMimeType("text/plain")) {
                        bodyText += bp.getContent().toString();
                    } else if (bp.isMimeType("text/html")) {
                        // could convert HTML to plain text
                    }
                }
            }
        }

        // save TicketMessage
        TicketMessage tm = TicketMessage.builder()
                .ticket(ticket)
                .body(bodyText)
                .emailMessageId(messageId)
                .inReplyTo(inReplyTo)
                .build();
        messageRepository.save(tm);
    }
}
