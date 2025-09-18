package com.InfraDesk.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyDTO {
    private String publicId;
    private String name;
    private String legalName;
    private String industry;
    private String gstNumber;
    private String contactEmail;
    private String contactPhone;
    private String address;
    private String logoUrl;
    private String domain;

    // Only domain names instead of whole CompanyDomain objects
    private List<String> domains;

    // Subsidiary IDs to prevent recursive nesting
    private List<CompanyDTO> subsidiaries;

    private Long parentCompanyId;
    private Long currentSubscriptionId;

    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;

    private Boolean isActive;
    private Boolean isDeleted;
}

