package com.InfraDesk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserMembershipDTO {
    private String userPublicId;
    private String email;
    private String username;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private List<MembershipInfoDTO> memberships;
}

