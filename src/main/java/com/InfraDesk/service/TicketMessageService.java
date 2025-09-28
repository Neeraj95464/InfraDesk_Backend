
package com.InfraDesk.service;

import com.InfraDesk.dto.PaginatedResponse;
import com.InfraDesk.dto.TicketMessageDTO;
import com.InfraDesk.dto.TicketMessageRequest;
import com.InfraDesk.entity.Ticket;
import com.InfraDesk.entity.TicketAttachment;
import com.InfraDesk.entity.TicketMessage;
import com.InfraDesk.entity.User;
import com.InfraDesk.exception.BusinessException;
import com.InfraDesk.mapper.TicketMessageMapper;
import com.InfraDesk.repository.TicketMessageRepository;
import com.InfraDesk.repository.TicketRepository;
import com.InfraDesk.repository.UserRepository;
import com.InfraDesk.util.AuthUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketMessageService {

    private final TicketMessageRepository ticketMessageRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final AuthUtils authUtils;
    private final TicketFileStorageService storageService;
    private final TicketFileStorageService fileStorageService; // you must create this to handle file saving

    @Transactional
    public TicketMessageDTO addMessage(TicketMessageRequest req, String companyId) throws Exception {
        // 1. Get current user (author)
        User creator = authUtils.getAuthenticatedUser()
                .orElseGet(() -> userRepository
                        .findByEmail(req.getSenderEmail())
                        .orElseThrow(() -> new BusinessException(
                                "User not found with email: " + req.getSenderEmail()
                        ))
                );

        // 2. Find ticket, author
        Ticket ticket = ticketRepository.findByPublicIdAndCompany_PublicId(req.getTicketId(), companyId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found for this company"));

        User author = userRepository.findByPublicId(creator.getPublicId())
                .orElseThrow(() -> new EntityNotFoundException("Author not found"));

        // 3. Create message
        TicketMessage message = TicketMessage.builder()
                .ticket(ticket)
                .author(author)
                .body(req.getBody())
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
                        .uploadedBy(author)
                        .ticketMessage(message)
                        .build();

                attachments.add(attachment);
            }
            message.setAttachments(attachments);
        }

        // 5. Save message (cascade saves attachments)
        ticketMessageRepository.save(message);

        // 6. Optionally send notifications here...

        // 7. Map to DTO
        return TicketMessageMapper.toDto(message);
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

