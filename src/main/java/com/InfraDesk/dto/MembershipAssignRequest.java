package com.InfraDesk.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class MembershipAssignRequest {

    @Email
    @NotBlank
    private String emailId;

    @NotBlank
    private String role;  // Use enum name like "COMPANY_CONFIGURE"
}
