package com.InfraDesk.dto;

import com.InfraDesk.enums.Role;
import lombok.Builder;

@Builder
public record CompanyMembershipDto(
        String id,             // publicId like "comp_xxx"
        String name,
        Role membershipRole  // OWNER, ADMIN, IT_ADMIN, EMPLOYEE
) {}
