package com.InfraDesk.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class TicketingDepartmentConfigCreateDTO {
    @NotNull(message = "Company publicId required")
    private String companyPublicId;

    @NotNull(message = "Department publicId required")
    private String departmentPublicId;

    private Boolean ticketEnabled = true;

    private String ticketEmail;

    private String note;

    private Boolean isActive = true;

    private Boolean isDeleted = false;
}
