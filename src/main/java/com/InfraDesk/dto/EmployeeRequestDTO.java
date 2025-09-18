package com.InfraDesk.dto;

import com.InfraDesk.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeRequestDTO {
    private String email;
    private String password;
    private String name;
    private String phone;
    private String employeeId;
    private String designation;
    private String departmentId;
    private String siteId;
    private String locationId;
    private Role role; // should match enum
}

