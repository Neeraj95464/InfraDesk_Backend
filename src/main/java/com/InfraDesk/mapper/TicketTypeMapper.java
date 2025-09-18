package com.InfraDesk.mapper;

import com.InfraDesk.dto.TicketTypeDTO;
import com.InfraDesk.entity.TicketType;

public class TicketTypeMapper {

    private TicketTypeMapper() {
        // utility class
    }

    public static TicketTypeDTO toDTO(TicketType entity) {
        if (entity == null) return null;

        return TicketTypeDTO.builder()
                .publicId(entity.getPublicId())
                .name(entity.getName())
                .description(entity.getDescription())
                .active(entity.getActive())
                .companyId(entity.getCompany().getPublicId() != null ? entity.getCompany().getPublicId() : null)

                .departmentId(entity.getDepartment() != null ? String.valueOf(entity.getDepartment().getId()) : null)
                .departmentName(entity.getDepartment() != null ? entity.getDepartment().getName() : null)

                .build();
    }

    public static TicketType toEntity(TicketTypeDTO dto) {
        if (dto == null) return null;

        TicketType entity = new TicketType();
        entity.setPublicId(dto.getPublicId());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setActive(dto.getActive());

        // ⚠️ Only set IDs in service layer (company, department, location will be fetched and set properly there)
        return entity;
    }
}
