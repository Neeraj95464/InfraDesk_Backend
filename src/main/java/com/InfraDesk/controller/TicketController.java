package com.InfraDesk.controller;

import com.InfraDesk.dto.CreateTicketRequest;
import com.InfraDesk.entity.Ticket;
import com.InfraDesk.entity.User;
import com.InfraDesk.exception.BusinessException;
import com.InfraDesk.service.TicketService;
import com.InfraDesk.util.AuthUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/companies/{companyId}/tickets")
public class TicketController {
    private final TicketService ticketService;
    private final AuthUtils authUtils;

    public TicketController(TicketService ticketService, AuthUtils authUtils) { this.ticketService = ticketService;
        this.authUtils = authUtils;
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> createTicket(@PathVariable String companyId,
                                          @RequestPart("payload") CreateTicketRequest payload,
                                          @RequestPart(name = "attachments", required = false) MultipartFile[] attachments) {
        try {
            // set attachments into payload
            payload.setAttachments(attachments == null ? null : java.util.Arrays.asList(attachments));
            Ticket t = ticketService.createTicket(payload, companyId);
            return ResponseEntity.ok(Map.of("publicId", t.getPublicId(), "ticketId", t.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
