
package com.InfraDesk.service;

import com.InfraDesk.dto.PaginatedResponse;
import com.InfraDesk.dto.TicketMessageDTO;
import com.InfraDesk.dto.TicketMessageRequest;
import com.InfraDesk.entity.*;
import com.InfraDesk.exception.BusinessException;
import com.InfraDesk.mapper.TicketMessageMapper;
import com.InfraDesk.repository.*;
import com.InfraDesk.util.AuthUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketMessageService {

    private static final Logger log = LoggerFactory.getLogger(TicketService.class);
    private final TicketMessageRepository ticketMessageRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final OutboundMailService outboundMailService;
    private final AuthUtils authUtils;
    private final EmployeeService employeeService;
    private final TicketingDepartmentConfigRepository ticketingDepartmentConfigRepository;
    private final MailIntegrationRepository mailIntegrationRepository;
    private final TicketFileStorageService storageService;
    private final CompanyRepository companyRepository;
    private final TicketFileStorageService fileStorageService; // you must create this to handle file saving

    @Transactional
    public TicketMessageDTO addMessage(TicketMessageRequest req, String companyId) throws Exception {
        // 1. Get current user (author)

        Company company = companyRepository.findByPublicId(companyId)
                .orElseThrow(()->new BusinessException("Company not found with id "+companyId));

        User creator;

        if (req.getSenderEmail() != null && !req.getSenderEmail().isBlank()) {
            // First try by email
            creator = userRepository.findByEmail(req.getSenderEmail())
                    .orElseGet(
                            ()->employeeService
                                    .createExternalUserWithMembership(companyId,req.getSenderEmail(),req.getSenderEmail())
                    );
        } else {
            // Fallback to authenticated user
            creator = authUtils.getAuthenticatedUser()
                    .orElseThrow(() -> new BusinessException("Authenticated user not found"));
        }

        // 2. Find ticket, author
        Ticket ticket = ticketRepository.findByPublicIdAndCompany_PublicId(req.getTicketId(), companyId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found for this company"));

//        User author = userRepository.findByPublicId(creator.getPublicId())
//                .orElseThrow(() -> new EntityNotFoundException("Author not found"));

        // 3. Create message
        TicketMessage message = TicketMessage.builder()
                .ticket(ticket)
                .author(creator)
                .body(req.getBody())
                .emailMessageId(req.getEmailMessageId())
                .inReplyTo(req.getInReplyTo())
                .internalNote(req.getInternalNote() != null ? req.getInternalNote() : Boolean.FALSE)
                .createdAt(LocalDateTime.now())
                .build();

        // 4. Handle attachments if present (save on disk + db row)
        if (req.getAttachments() != null && !req.getAttachments().isEmpty()) {
            List<TicketAttachment> attachments = new ArrayList<>();
            for (MultipartFile file : req.getAttachments()) {
                String storedFilePath;
                try {
                    storedFilePath = storageService.storeFile(file, ticket.getPublicId());
                } catch (IOException e) {
                    e.printStackTrace();
                    throw e;
                }

                TicketAttachment attachment = TicketAttachment.builder()
                        .ticket(ticket)
                        .originalFileName(file.getOriginalFilename())
                        .filePath(storedFilePath)
                        .sizeInBytes(file.getSize())
                        .contentType(file.getContentType())
                        .uploadedBy(creator)
                        .ticketMessage(message)
                        .build();

                attachments.add(attachment);
            }
            message.setAttachments(attachments);
        }

        // 5. Save message (cascade saves attachments)
        ticketMessageRepository.save(message);

        sendTicketMessageAckMail(ticket,creator,message,company);

        // 6. Optionally send notifications here...

        // 7. Map to DTO
        return TicketMessageMapper.toDto(message);
    }


    public void sendTicketMessageAckMail(Ticket ticket,User author, TicketMessage message, Company company) {
        try {
            TicketingDepartmentConfig ticketingEmail = ticketingDepartmentConfigRepository
                    .findByCompanyAndDepartment(company, ticket.getDepartment())
                    .orElseThrow(() -> new BusinessException("Ticketing department not found"));

            MailIntegration mailIntegration = mailIntegrationRepository
                    .findByCompanyIdAndMailboxEmail(company.getPublicId(), ticketingEmail.getTicketEmail())
                    .orElseThrow(() -> new BusinessException("Associated mail not found for department " + ticketingEmail.getTicketEmail()));

            if (mailIntegration == null || !Boolean.TRUE.equals(mailIntegration.getEnabled())) {
                log.warn("No active mail integration found for company {}, skipping notification email", company.getPublicId());
                return;
            }
            List<String> toEmails;

            if (ticket.getCreatedBy() != null && ticket.getCreatedBy().equals(author)) {
                toEmails = (ticket.getAssignee() != null
                        && ticket.getAssignee().getEmail() != null
                        && !ticket.getAssignee().getEmail().isBlank())
                        ? Collections.singletonList(ticket.getAssignee().getEmail())
                        : Collections.emptyList();
            } else {
                toEmails = (ticket.getCreatedBy() != null
                        && ticket.getCreatedBy().getEmail() != null
                        && !ticket.getCreatedBy().getEmail().isBlank())
                        ? Collections.singletonList(ticket.getCreatedBy().getEmail())
                        : Collections.emptyList();
            }
            System.out.println("these are to mails "+toEmails);


            // Prepare recipients
//            List<String> toEmails = Collections.singletonList(ticket.getCreatedBy().getEmail());
//             = Collections.singletonList(ticket.getCreatedBy().getEmail());

            // CC → assignee if available
//            List<String> ccEmails = (ticket.getAssignee() != null && ticket.getAssignee().getEmail() != null && !ticket.getAssignee().getEmail().isBlank())
//                    ? Collections.singletonList(ticket.getAssignee().getEmail())
//                    : Collections.emptyList();
            List<String> ccEmails = null;

            // Subject remains the ticket subject with ID
            String subject = String.format("Re: Ticket [%s] %s", ticket.getPublicId(), ticket.getSubject());

            // Body → reflect new message
            String htmlBody = String.format(
                    "<p>Dear %s,</p>" +
                            "<p>A new message has been added to your ticket <b>%s</b>.</p>" +
                            "<p><b>Message:</b><br/>%s</p>" +
                            "<p>Ticket Status: %s</p>" +
                            "<hr/><p>This is an automated acknowledgement from %s Support.</p>",
                    getUserDisplayName(ticket.getCreatedBy()),
                    ticket.getPublicId(),
                    message.getBody().replace("\n", "<br/>"),
                    ticket.getStatus(),
                    company.getName()
            );

            outboundMailService.sendGmailMessage(mailIntegration,ticket, toEmails, ccEmails, subject, htmlBody);
            log.info("Message acknowledgement email sent for ticket {}", ticket.getPublicId());
        } catch (Exception mailEx) {
            log.error("Failed to send ticket message acknowledgement for ticket {}: {}", ticket.getPublicId(), mailEx.getMessage(), mailEx);
        }
    }

    public String getUserDisplayName(User user) {
        if (user == null) return "";
        if (user.getEmployeeProfiles() != null && !user.getEmployeeProfiles().isEmpty()) {
            return user.getEmployeeProfiles().get(0).getName();
        }
        return user.getEmail();
    }


    public PaginatedResponse<TicketMessageDTO> getMessagesByTicket(String companyId, String ticketId, Pageable pageable) {
        Page<TicketMessage> page = ticketMessageRepository.findByTicket_PublicIdAndTicket_Company_PublicIdOrderByCreatedAtAsc(
                ticketId, companyId, pageable
        );

        return new PaginatedResponse<>(
                page.map(TicketMessageMapper::toDto).getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    private TicketAttachment saveAttachment(MultipartFile file, TicketMessage message) {
        try {
            String fileUrl = fileStorageService.storeFile(file, "tickets/" + message.getTicket().getPublicId());
            return TicketAttachment.builder()
                    .ticketMessage(message)
                    .originalFileName(file.getOriginalFilename())
//                    .fileUrl(fileUrl)
                    .contentType(file.getContentType())
                    .sizeInBytes(file.getSize())
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save attachment: " + file.getOriginalFilename(), e);
        }
    }
}

