package com.InfraDesk.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanySettingsDTO {
    private Integer ticketDefaultDueDays;
    private String companyShortCode;
    private Boolean assetTagRequired;
}

