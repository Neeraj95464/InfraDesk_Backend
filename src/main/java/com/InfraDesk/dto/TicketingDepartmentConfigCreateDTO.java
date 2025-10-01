package com.InfraDesk.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;


@Data
public class TicketingDepartmentConfigCreateDTO {
    @NotNull(message = "Company publicId required")
    private String companyPublicId;

    @NotNull(message = "Department publicId required")
    private String departmentPublicId;

    private Boolean ticketEnabled = true;

    private String ticketEmail;

    private String note;
    private Boolean allowTicketsFromAnyDomain;
    private Set<String> allowedDomainsForTicket;
    private Boolean isActive = true;

    private Boolean isDeleted = false;
}
