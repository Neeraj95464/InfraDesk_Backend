package com.InfraDesk.dto;

import com.InfraDesk.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeResponseDTO {
    private String email;
    private String name;
    private String phone;
    private String employeeId;
    private String publicId;
    private String designation;
    private Long companyId;
    private String companyName;
    private Long departmentId;
    private String departmentName;
    private Long siteId;
    private String siteName;
    private Long locationId;
    private String locationName;
    private LocalDateTime createdAt;
    private String createdBy;
    private boolean isActive;
    private boolean isDeleted;
}
