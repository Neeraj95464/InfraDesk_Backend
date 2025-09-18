package com.InfraDesk.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record AuthResponse(
        UserInfoDto user,                 // authenticated user
        List<CompanyMembershipDto> companies, // tenant memberships
        PreferencesDto preferences        // user preferences
) {}
