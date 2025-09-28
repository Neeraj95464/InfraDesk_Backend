package com.InfraDesk.dto;

import lombok.Data;

@Data
public class TicketingDepartmentConfigDTO {
    private String publicId;
    private String companyPublicId;
    private String departmentPublicId;
    private String departmentName;
    private Boolean ticketEnabled;
    private String ticketEmail;
    private String note;
    private Boolean isActive;
    private Boolean isDeleted;
    private String createdBy;
    private String updatedBy;
    private String createdAt;  // Format as ISO string
    private String updatedAt;  // Format as ISO string
}
