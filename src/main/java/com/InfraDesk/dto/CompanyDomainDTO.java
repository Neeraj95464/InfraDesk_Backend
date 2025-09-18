package com.InfraDesk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CompanyDomainDTO {
    private String publicId;   // public UUID
    private String domain;
    private Boolean isActive;
}
