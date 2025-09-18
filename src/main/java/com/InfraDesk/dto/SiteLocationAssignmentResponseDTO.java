package com.InfraDesk.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteLocationAssignmentResponseDTO {
    private Long id;
    private String companyPublicId;
    private String sitePublicId;
    private String siteName;
    private String locationPublicId;
    private String locationName;
    private Boolean isActive;
    private String createdBy;
    private LocalDateTime createdAt;
}

