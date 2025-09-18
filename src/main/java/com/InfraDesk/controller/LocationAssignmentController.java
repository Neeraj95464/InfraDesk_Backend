package com.InfraDesk.controller;

import com.InfraDesk.dto.LocationAssignmentRequest;
import com.InfraDesk.entity.LocationAssignment;
import com.InfraDesk.service.LocationAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies/{companyId}/locations/{locationPublicId}/assignments")
@RequiredArgsConstructor
public class LocationAssignmentController {

    private final LocationAssignmentService locationAssignmentService;

    @PostMapping
    @PreAuthorize("@perm.check(#companyId, 'COMPANY_CONFIGURE')")
    public ResponseEntity<?> assignLocationToSite(
            @PathVariable String companyId,
            @PathVariable String locationPublicId,
            @RequestBody LocationAssignmentRequest request
    ) {
        LocationAssignment savedAssignment = locationAssignmentService.assignLocationToSite(
                companyId,
                locationPublicId,
                request.getSitePublicId(),
                request.getDepartmentId(),
                request.getExecutiveId(),
                request.getManagerId()
        );
        return ResponseEntity.ok(savedAssignment);
    }
}

