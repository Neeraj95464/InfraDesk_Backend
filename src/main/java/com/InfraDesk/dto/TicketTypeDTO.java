package com.InfraDesk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketTypeDTO {
    private String publicId;
    private String name;
    private String description;
    private Boolean active;

    private String companyId;
    private String departmentId;
    private String departmentName;
}
