package com.InfraDesk.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUserDTO {
    private Long userId;       // Global user ID
    private String employeeId; // Employee code inside company
    private String username;   // Full name (or fallback to email)
    private String email;      // User email (global identity)
    private Long companyId;    // Tenant company ID
    private String role;       // Company role (optional, but recommended)
}
