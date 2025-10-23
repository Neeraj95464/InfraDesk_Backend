package com.InfraDesk.mapper;

import com.InfraDesk.dto.MembershipInfoDTO;
import com.InfraDesk.entity.Membership;

public class FilteredMembershipMapper {
    public static MembershipInfoDTO toDTO(Membership m) {
        return MembershipInfoDTO.builder()
                .companyPublicId(m.getCompany().getPublicId())
                .companyName(m.getCompany().getName())
                .role(m.getRole())
                .isActive(m.getIsActive())
                .createdAt(m.getCreatedAt())
                .build();
    }
}

