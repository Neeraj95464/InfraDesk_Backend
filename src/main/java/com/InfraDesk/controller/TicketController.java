package com.InfraDesk.controller;

import com.InfraDesk.dto.CreateTicketRequest;
import com.InfraDesk.dto.PaginatedResponse;
import com.InfraDesk.dto.TicketDTO;
import com.InfraDesk.dto.TicketFilterRequest;
import com.InfraDesk.entity.Ticket;
import com.InfraDesk.entity.User;
import com.InfraDesk.enums.TicketStatus;
import com.InfraDesk.exception.BusinessException;
import com.InfraDesk.service.TicketService;
import com.InfraDesk.util.AuthUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
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
    @PreAuthorize("@perm.check(#companyId, 'TICKET_VIEW')")
    public ResponseEntity<?> createTicket(@PathVariable String companyId,
                                          @RequestPart("payload") CreateTicketRequest payload,
                                          @RequestPart(name = "attachments", required = false) MultipartFile[] attachments) {
//        System.out.println("Attachments received: " + (attachments == null ? 0 : attachments.length));
        try {
            // set attachments into payload
            payload.setAttachments(attachments == null ? null : java.util.Arrays.asList(attachments));
            Ticket t = ticketService.createTicket(payload, companyId);
            return ResponseEntity.ok(Map.of("publicId", t.getPublicId(), "ticketId", t.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/import")
    @PreAuthorize("@perm.check(#companyId, 'TICKET_ADMIN')")
    public ResponseEntity<TicketService.ImportResult> importTickets(
            @PathVariable String companyId,
            @RequestParam("file") MultipartFile file) throws Exception {

        TicketService.ImportResult importResult = ticketService.importTicketsFromExcel(file.getInputStream(), companyId);
        return ResponseEntity.ok(importResult);
    }


    @GetMapping("/filter")
    @PreAuthorize("@perm.check(#companyId, 'TICKET_VIEW')")
    public PaginatedResponse<TicketDTO> filterTickets(
            @ModelAttribute TicketFilterRequest req,
            @RequestParam String companyId
    ) {

        return ticketService.filterTickets(req, companyId);
    }

    @GetMapping("/filter/export")
    @PreAuthorize("@perm.check(#companyId, 'TICKET_VIEW')")
    public void exportFilteredTickets(
            TicketFilterRequest req, // Spring will auto-bind query params to this request object
            @RequestParam String companyId,
            HttpServletResponse response
    ) throws IOException {
        List<TicketDTO> tickets = ticketService.filterTicketsNoPaging(req, companyId);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=FilteredTickets.xlsx");

        ticketService.writeTicketsToExcel(tickets, response.getOutputStream());
    }

    // ✅ List all tickets (paginated)
    @GetMapping
    @PreAuthorize("@perm.check(#companyId, 'TICKET_VIEW')")
    public ResponseEntity<PaginatedResponse<TicketDTO>> getTickets(
            @PathVariable String companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {

        PaginatedResponse<TicketDTO> response = (search == null || search.isBlank())
                ? ticketService.getAllTickets(companyId, page, size)
                : ticketService.searchTickets(companyId, search, page, size);

        return ResponseEntity.ok(response);
    }

    // ✅ Get ticket by publicId
    @GetMapping("/{publicId}")
    @PreAuthorize("@perm.check(#companyId, 'TICKET_VIEW')")
    public ResponseEntity<?> getTicket(@PathVariable String companyId, @PathVariable String publicId) {
        return ticketService.getTicketByPublicId(companyId, publicId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @PutMapping("/{id}")
    @PreAuthorize("@perm.check(#companyId, 'TICKET_MANAGE')")
    public ResponseEntity<TicketDTO> updateTicket(
            @PathVariable String companyId,
            @PathVariable String id,
            @RequestBody TicketDTO dto) {
        return ResponseEntity.ok(ticketService.updateTicket(id, companyId, dto));
    }

    @PutMapping("/{id}/priority-status")
    @PreAuthorize("@perm.check(#companyId, 'TICKET_VIEW')")
    public ResponseEntity<TicketDTO> updatePriorityAndStatus(
            @PathVariable String companyId,
            @PathVariable String id,
            @RequestBody TicketDTO dto) {
        return ResponseEntity.ok(ticketService.updatePriorityAndStatusForViewUser(id, companyId, dto));
    }

    // ✅ Delete ticket
    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.check(#companyId, 'TICKET_ADMIN')")
    public ResponseEntity<?> deleteTicket(
            @PathVariable String companyId, @PathVariable String id) {
        ticketService.deleteTicket(id,companyId);
        return ResponseEntity.noContent().build();
    }
}
