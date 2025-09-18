package com.InfraDesk.controller;

import com.InfraDesk.dto.SiteLocationAssignmentResponseDTO;
import com.InfraDesk.entity.SiteLocationAssignment;
import com.InfraDesk.service.SiteLocationAssignmentService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies/{companyId}/locations/{locationPublicId}/site-links")
@RequiredArgsConstructor
public class SiteLocationAssignmentController {

    private final SiteLocationAssignmentService assignmentService;

    @PostMapping
    @PreAuthorize("@perm.check(#companyId, 'COMPANY_CONFIGURE')")
    public ResponseEntity<?> linkLocationToSite(
            @PathVariable String companyId,
            @PathVariable String locationPublicId,
            @RequestBody LinkLocationRequest request
    ) {
         SiteLocationAssignmentResponseDTO assignment = assignmentService.linkLocationToSite(
                companyId, locationPublicId, request.getSitePublicId()
        );
        return ResponseEntity.ok(assignment);
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class LinkLocationRequest {
    private String sitePublicId;
}
