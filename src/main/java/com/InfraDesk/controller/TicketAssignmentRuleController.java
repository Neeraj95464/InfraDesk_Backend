


package com.InfraDesk.controller;

import com.InfraDesk.dto.TicketAssignmentRuleDTO;
import com.InfraDesk.dto.PaginatedResponse;
import com.InfraDesk.service.TicketAssignmentRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies/{companyId}/assignment-rules")
@RequiredArgsConstructor
public class TicketAssignmentRuleController {

    private final TicketAssignmentRuleService ruleService;

    /**
     * Create or update a rule for a company.
     * Accepts a TicketAssignmentRuleDTO from the frontend.
     */
    @PostMapping
    public ResponseEntity<TicketAssignmentRuleDTO> createOrUpdateRule(
            @PathVariable String companyId,
            @RequestBody TicketAssignmentRuleDTO ruleDTO
    ) {
        TicketAssignmentRuleDTO savedRule = ruleService.saveRule(companyId, ruleDTO);
        return ResponseEntity.ok(savedRule);
    }

    /**
     * Get all rules for a company with pagination.
     */
    @GetMapping
    public ResponseEntity<PaginatedResponse<TicketAssignmentRuleDTO>> getRules(
            @PathVariable String companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<TicketAssignmentRuleDTO> content = ruleService.getRulesByCompany(companyId);
        long totalElements = ruleService.countRulesByCompany(companyId);
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean last = page + 1 >= totalPages;

        PaginatedResponse<TicketAssignmentRuleDTO> response = new PaginatedResponse<>(
                content,
                page,
                size,
                totalElements,
                totalPages,
                last
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get a single rule by ID
     */
    @GetMapping("/{ruleId}")
    public ResponseEntity<TicketAssignmentRuleDTO> getRule(
            @PathVariable String companyId,
            @PathVariable Long ruleId
    ) {
        TicketAssignmentRuleDTO ruleDTO = ruleService.getRule(companyId, ruleId);
        return ResponseEntity.ok(ruleDTO);
    }

    /**
     * Delete a rule by ID
     */
    @DeleteMapping("/{ruleId}")
    public ResponseEntity<Void> deleteRule(
            @PathVariable String companyId,
            @PathVariable Long ruleId
    ) {
        ruleService.deleteRule(companyId, ruleId);
        return ResponseEntity.noContent().build();
    }
}
