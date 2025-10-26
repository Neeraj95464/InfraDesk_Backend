package com.InfraDesk.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CompanyAssetTypeDTO {
    private Long id;
    private String typeName;
    private String description;
}
