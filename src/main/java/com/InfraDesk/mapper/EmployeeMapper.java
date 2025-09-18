package com.InfraDesk.mapper;

import com.InfraDesk.dto.EmployeeResponseDTO;
import com.InfraDesk.entity.Employee;

public class EmployeeMapper {

    public static EmployeeResponseDTO toDTO(Employee e) {
        return new EmployeeResponseDTO(
                e.getUser().getEmail(),
                e.getName(),
                e.getPhone(),
                e.getEmployeeId(),
                e.getPublicId(),
                e.getDesignation(),
                e.getCompany() != null ? e.getCompany().getId() : null,
                e.getCompany() != null ? e.getCompany().getName() : null,
                e.getDepartment() != null ? e.getDepartment().getId() : null,
                e.getDepartment() != null ? e.getDepartment().getName() : null,
                e.getSite() != null ? e.getSite().getId() : null,
                e.getSite() != null ? e.getSite().getName() : null,
                e.getLocation() != null ? e.getLocation().getId() : null,
                e.getLocation() != null ? e.getLocation().getName() : null,
                e.getCreatedAt(),
                e.getCreatedBy(),
                e.getIsActive(),
                e.getIsDeleted()
        );
    }
}

