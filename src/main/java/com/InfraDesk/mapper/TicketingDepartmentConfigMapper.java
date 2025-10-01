package com.InfraDesk.mapper;

import com.InfraDesk.dto.TicketingDepartmentConfigCreateDTO;
import com.InfraDesk.dto.TicketingDepartmentConfigDTO;
import com.InfraDesk.entity.Company;
import com.InfraDesk.entity.Department;
import com.InfraDesk.entity.TicketingDepartmentConfig;

import java.time.format.DateTimeFormatter;

public class TicketingDepartmentConfigMapper {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static TicketingDepartmentConfigDTO toDto(TicketingDepartmentConfig entity) {
        if (entity == null) return null;

        TicketingDepartmentConfigDTO dto = new TicketingDepartmentConfigDTO();
        dto.setPublicId(entity.getPublicId());
        dto.setCompanyPublicId(entity.getCompany() != null ? entity.getCompany().getPublicId() : null);
        dto.setDepartmentPublicId(entity.getDepartment() != null ? entity.getDepartment().getPublicId() : null);
        dto.setDepartmentName(entity.getDepartment() != null ? entity.getDepartment().getName() : null);
        dto.setTicketEnabled(entity.getTicketEnabled());
        dto.setTicketEmail(entity.getTicketEmail());
        dto.setNote(entity.getNote());
        dto.setAllowTicketsFromAnyDomain(entity.getAllowTicketsFromAnyDomain());
        dto.setAllowedDomainsForTicket(entity.getAllowedTicketDomains());
        dto.setIsActive(entity.getIsActive());
        dto.setIsDeleted(entity.getIsDeleted());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt().format(formatter) : null);
        dto.setUpdatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().format(formatter) : null);

        return dto;
    }

    public static TicketingDepartmentConfig toEntity(TicketingDepartmentConfigCreateDTO dto) {
        if (dto == null) return null;

        TicketingDepartmentConfig entity = new TicketingDepartmentConfig();
        entity.setCompany(new Company());
        entity.getCompany().setPublicId(dto.getCompanyPublicId());
        entity.setDepartment(new Department());
        entity.getDepartment().setPublicId(dto.getDepartmentPublicId());
        entity.setTicketEnabled(dto.getTicketEnabled());
        entity.setTicketEmail(dto.getTicketEmail());
        entity.setNote(dto.getNote());
        entity.setIsActive(dto.getIsActive());
        entity.setIsDeleted(dto.getIsDeleted());

        return entity;
    }
}
