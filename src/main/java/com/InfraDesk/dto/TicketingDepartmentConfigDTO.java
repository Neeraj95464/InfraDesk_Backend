package com.InfraDesk.dto;

import lombok.Data;

import java.util.Set;

@Data
public class TicketingDepartmentConfigDTO {
    private String publicId;
    private String companyPublicId;
    private String departmentPublicId;
    private String departmentName;
    private Boolean ticketEnabled;
    private String ticketEmail;
    private String note;
    private Boolean allowTicketsFromAnyDomain;
    private Set<String> allowedDomainsForTicket;
    private Boolean isActive;
    private Boolean isDeleted;
    private String createdBy;
    private String updatedBy;
    private String createdAt;  // Format as ISO string
    private String updatedAt;  // Format as ISO string
}
