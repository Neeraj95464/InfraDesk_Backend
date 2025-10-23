package com.InfraDesk.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class EmployeeFilterRequest {
    private Boolean isActive;
    private Boolean isDeleted;
    private String nameOrEmail; // searchable field (name or email)
    private String departmentName;
    private String designation;
    private String siteName;
    private String locationName;
    private String createdBy;
    private LocalDateTime createdAfter;
    private LocalDateTime createdBefore;
}
