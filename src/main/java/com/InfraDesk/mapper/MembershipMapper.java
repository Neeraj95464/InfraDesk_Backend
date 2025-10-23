package com.InfraDesk.mapper;

import com.InfraDesk.dto.MembershipInfoDTO;
import com.InfraDesk.entity.Membership;

public class MembershipMapper {

    public static MembershipInfoDTO toDTO(Membership membership) {
        if (membership == null) return null;

        return MembershipInfoDTO.builder()
                .companyPublicId(membership.getCompany().getPublicId())
                .companyName(membership.getCompany().getName())
                .role(membership.getRole())
                .isActive(membership.getIsActive())
                .createdAt(membership.getCreatedAt())
                .build();
    }
}


