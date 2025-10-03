package com.InfraDesk.util;

import com.InfraDesk.dto.TicketMessageDTO;
import com.InfraDesk.dto.TicketMessageRequest;
import com.InfraDesk.entity.*;
import com.InfraDesk.exception.BusinessException;
import com.InfraDesk.mapper.TicketMessageMapper;
import com.InfraDesk.repository.TicketMessageRepository;
import com.InfraDesk.repository.TicketRepository;
import com.InfraDesk.repository.UserRepository;
import com.InfraDesk.service.EmployeeService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
public class TicketMessageHelper {

    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final TicketMessageRepository ticketMessageRepository;
    private final AuthUtils authUtils;
    private final EmployeeService employeeService;

    @Transactional
    public TicketMessageDTO addMessage(TicketMessageRequest req, String companyId) {
        // 1. Resolve author
        User author;

        if (req.getSenderEmail() != null && !req.getSenderEmail().isBlank()) {
            // First try by email
            author = userRepository.findByEmail(req.getSenderEmail())
                    .orElseGet(
                            ()->employeeService
                                    .createExternalUserWithMembership(companyId,req.getSenderEmail(),req.getSenderEmail())
                    );
        } else {
            // Fallback to authenticated user
            author = authUtils.getAuthenticatedUser()
                    .orElseThrow(() -> new BusinessException("Authenticated user not found"));
        }

        // 2. Find ticket
        Ticket ticket = ticketRepository.findByPublicIdAndCompany_PublicId(req.getTicketId(), companyId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found for this company"));

        // 3. Create message
        TicketMessage message = TicketMessage.builder()
                .ticket(ticket)
                .author(author)
                .body(req.getBody())
                .internalNote(Boolean.TRUE.equals(req.getInternalNote()))
                .emailMessageId(req.getEmailMessageId())
                .inReplyTo(req.getInReplyTo())
                .createdAt(LocalDateTime.now())
                .build();

        // 4. Save message
        ticketMessageRepository.save(message);

        // 5. Return DTO
        return TicketMessageMapper.toDto(message);
    }
}

