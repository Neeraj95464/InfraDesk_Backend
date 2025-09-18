package com.InfraDesk.dto;


import com.InfraDesk.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MembershipInfoDTO {
    private String companyPublicId;
    private String companyName;
    private Role role;
    private boolean isActive;
    private LocalDateTime createdAt;
}



