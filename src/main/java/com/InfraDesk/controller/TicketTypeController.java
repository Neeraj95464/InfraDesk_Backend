package com.InfraDesk.controller;

import com.InfraDesk.dto.PaginatedResponse;
import com.InfraDesk.dto.TicketTypeDTO;
import com.InfraDesk.entity.TicketType;
import com.InfraDesk.service.TicketTypeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies/{companyId}/tickettypes")
public class TicketTypeController {

    private final TicketTypeService ticketTypeService;

    public TicketTypeController(TicketTypeService ticketTypeService) {
        this.ticketTypeService = ticketTypeService;
    }

    @GetMapping
    @PreAuthorize("@perm.check(#companyId, 'COMPANY_CONFIGURE')")
    public ResponseEntity<PaginatedResponse<TicketTypeDTO>> getAllTicketTypes(
            @PathVariable String companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {

        PaginatedResponse<TicketTypeDTO> ticketTypes =
                ticketTypeService.getAllByCompany(companyId, page, size, search);

        return ResponseEntity.ok(ticketTypes);
    }


    @GetMapping("/{id}")
    @PreAuthorize("@perm.check(#companyId, 'COMPANY_CONFIGURE')")
    public ResponseEntity<TicketTypeDTO> getTicketTypeById(@PathVariable String companyId, @PathVariable String id) {
        return ticketTypeService.getByIdAndCompany(id, companyId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("@perm.check(#companyId, 'COMPANY_CONFIGURE')")
    public ResponseEntity<?> createTicketType(
            @PathVariable String companyId,
            @Valid @RequestBody TicketTypeDTO ticketType) {

        TicketTypeDTO created = ticketTypeService.createTicketType(companyId, ticketType);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.check(#companyId, 'COMPANY_CONFIGURE')")
    public ResponseEntity<?> updateTicketType(
            @PathVariable String companyId,
            @PathVariable String id,
            @Valid @RequestBody TicketTypeDTO ticketType) {
        try {
            TicketTypeDTO updated = ticketTypeService.updateTicketType(companyId, id, ticketType);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.check(#companyId, 'COMPANY_CONFIGURE')")
    public ResponseEntity<?> deleteTicketType(@PathVariable String companyId, @PathVariable String id) {
        try {
            ticketTypeService.deleteTicketType(companyId, id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}


