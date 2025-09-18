//package com.InfraDesk.service;
//
//import com.InfraDesk.entity.Company;
//import com.InfraDesk.entity.Department;
//import com.InfraDesk.entity.Location;
//import com.InfraDesk.entity.TicketType;
//import com.InfraDesk.repository.CompanyRepository;
//import com.InfraDesk.repository.DepartmentRepository;
//import com.InfraDesk.repository.LocationRepository;
//import com.InfraDesk.repository.TicketTypeRepository;
//import jakarta.transaction.Transactional;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.stereotype.Service;
//import org.springframework.util.StringUtils;
//
//import java.util.List;
//import java.util.Optional;
//
//@Service
//public class TicketTypeService {
//
//    private final TicketTypeRepository ticketTypeRepository;
//    private final CompanyRepository companyRepository;
//    private final LocationRepository locationRepository;
//    private final DepartmentRepository departmentRepository;
//
//    public TicketTypeService(TicketTypeRepository ticketTypeRepository, CompanyRepository companyRepository, LocationRepository locationRepository, DepartmentRepository departmentRepository) {
//        this.ticketTypeRepository = ticketTypeRepository;
//        this.companyRepository = companyRepository;
//        this.locationRepository = locationRepository;
//        this.departmentRepository = departmentRepository;
//    }
//
////    public List<TicketType> getAllByCompany(String companyId) {
////        return ticketTypeRepository.findByCompanyPublicIdAndActiveTrueAndCompanyIsActiveTrue(companyId);
////    }
//
//    public Page<TicketType> getAllByCompany(String companyPublicId, int page, int size, String search) {
//        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
//
//        if (search != null && !search.isBlank()) {
//            return ticketTypeRepository.findByCompanyPublicIdAndNameContainingIgnoreCaseAndActiveTrue(
//                    companyPublicId, search, pageable);
//        }
//
//        return ticketTypeRepository.findByCompanyPublicIdAndActiveTrue(companyPublicId, pageable);
//    }
//
//
//    public Optional<TicketType> getByIdAndCompany(Long id, Long companyId) {
//        return ticketTypeRepository.findByIdAndCompanyId(id, companyId);
//    }
//
//    @Transactional
//    public TicketType createTicketType(String companyId, TicketType input) {
//        Company company = companyRepository.findByPublicId(companyId)
//                .orElseThrow(() -> new IllegalArgumentException("Company not found"));
//
//        if (input.getDepartment() == null || input.getDepartment().getId() == null) {
//            throw new IllegalArgumentException("Department is required");
//        }
//
//        // Fetch Department entity
//        Department department = departmentRepository.findById(input.getDepartment().getId())
//                .orElseThrow(() -> new IllegalArgumentException("Department not found"));
//
//        Location location = null;
//        if (input.getLocation() != null && input.getLocation().getId() != null) {
//            location = locationRepository.findById(input.getLocation().getId())
//                    .orElseThrow(() -> new IllegalArgumentException("Location not found"));
//        }
//
//        validateTicketTypeNameUniqueness(company.getId(), input.getName());
//
//        TicketType ticketType = TicketType.builder()
//                .company(company)
//                .department(department)
//                .location(location)
//                .name(StringUtils.capitalize(input.getName().trim()))
//                .description(input.getDescription())
//                .active(input.getActive() != null ? input.getActive() : Boolean.TRUE)
//                .build();
//
//        return ticketTypeRepository.save(ticketType);
//    }
//
//
//
//    @Transactional
//    public TicketType updateTicketType(Long companyId, Long ticketTypeId, TicketType input) {
//        TicketType existing = ticketTypeRepository.findByIdAndCompanyId(ticketTypeId, companyId)
//                .orElseThrow(() -> new IllegalArgumentException("Ticket type not found"));
//
//        if (!existing.getName().equalsIgnoreCase(input.getName())) {
//            validateTicketTypeNameUniqueness(companyId, input.getName());
//            existing.setName(StringUtils.capitalize(input.getName().trim()));
//        }
//        existing.setDescription(input.getDescription());
//        if (input.getActive() != null) {
//            existing.setActive(input.getActive());
//        }
//
//        return ticketTypeRepository.save(existing);
//    }
//
//    @Transactional
//    public void deleteTicketType(Long companyId, Long ticketTypeId) {
//        TicketType existing = ticketTypeRepository.findByIdAndCompanyId(ticketTypeId, companyId)
//                .orElseThrow(() -> new IllegalArgumentException("Ticket type not found"));
//        ticketTypeRepository.delete(existing);
//    }
//
//    private void validateTicketTypeNameUniqueness(Long companyId, String name) {
//        boolean exists = ticketTypeRepository.existsByCompanyIdAndNameIgnoreCase(companyId, name);
//        if (exists) {
//            throw new IllegalArgumentException("Ticket type with this name already exists for the company");
//        }
//    }
//}
//



package com.InfraDesk.service;

import com.InfraDesk.dto.PaginatedResponse;
import com.InfraDesk.dto.TicketTypeDTO;
import com.InfraDesk.entity.Company;
import com.InfraDesk.entity.Department;
import com.InfraDesk.entity.Location;
import com.InfraDesk.entity.TicketType;
import com.InfraDesk.mapper.TicketTypeMapper;
import com.InfraDesk.repository.CompanyRepository;
import com.InfraDesk.repository.DepartmentRepository;
import com.InfraDesk.repository.LocationRepository;
import com.InfraDesk.repository.TicketTypeRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class TicketTypeService {

    private final TicketTypeRepository ticketTypeRepository;
    private final CompanyRepository companyRepository;
    private final LocationRepository locationRepository;
    private final DepartmentRepository departmentRepository;

    public TicketTypeService(
            TicketTypeRepository ticketTypeRepository,
            CompanyRepository companyRepository,
            LocationRepository locationRepository,
            DepartmentRepository departmentRepository
    ) {
        this.ticketTypeRepository = ticketTypeRepository;
        this.companyRepository = companyRepository;
        this.locationRepository = locationRepository;
        this.departmentRepository = departmentRepository;
    }

    /**
     * Fetch all ticket types for a company with pagination and optional search
     */
    public PaginatedResponse<TicketTypeDTO> getAllByCompany(String companyPublicId, int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());

        Page<TicketType> ticketTypes;
        if (StringUtils.hasText(search)) {
            ticketTypes = ticketTypeRepository
                    .findByCompanyPublicIdAndNameContainingIgnoreCaseAndActiveTrue(companyPublicId, search, pageable);
        } else {
            ticketTypes = ticketTypeRepository
                    .findByCompanyPublicIdAndActiveTrue(companyPublicId, pageable);
        }

        List<TicketTypeDTO> dtos = ticketTypes.map(TicketTypeMapper::toDTO).getContent();

        return new PaginatedResponse<>(
                dtos,
                ticketTypes.getNumber(),
                ticketTypes.getSize(),
                ticketTypes.getTotalElements(),
                ticketTypes.getTotalPages(),
                ticketTypes.isLast()
        );
    }

    /**
     * Get ticket type by ID (within company)
     */
    public Optional<TicketTypeDTO> getByIdAndCompany(String id, String companyId) {
        return ticketTypeRepository.findByPublicIdAndCompanyPublicId(id, companyId)
                .map(TicketTypeMapper::toDTO);
    }

    /**
     * Create new TicketType
     */
    @Transactional
    public TicketTypeDTO createTicketType(String companyPublicId, TicketTypeDTO dto) {
        Company company = companyRepository.findByPublicId(companyPublicId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        if (dto.getDepartmentId() == null) {
            throw new IllegalArgumentException("Department is required");
        }

        Department department = departmentRepository.findByPublicIdAndCompany_PublicId(dto.getDepartmentId(),companyPublicId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));

        validateTicketTypeNameUniqueness(company.getPublicId(), dto.getName());

        TicketType ticketType = TicketType.builder()
                .company(company)
                .department(department)
                .name(StringUtils.capitalize(dto.getName().trim()))
                .description(dto.getDescription())
                .active(dto.getActive() != null ? dto.getActive() : Boolean.TRUE)
                .build();

        return TicketTypeMapper.toDTO(ticketTypeRepository.save(ticketType));
    }

    /**
     * Update TicketType
     */
    @Transactional
    public TicketTypeDTO updateTicketType(String companyId, String ticketTypeId, TicketTypeDTO dto) {
        TicketType existing = ticketTypeRepository.findByPublicIdAndCompanyPublicId(ticketTypeId, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket type not found"));

        if (!existing.getName().equalsIgnoreCase(dto.getName())) {
            validateTicketTypeNameUniqueness(companyId, dto.getName());
            existing.setName(StringUtils.capitalize(dto.getName().trim()));
        }

        existing.setDescription(dto.getDescription());
        if (dto.getActive() != null) {
            existing.setActive(dto.getActive());
        }

        return TicketTypeMapper.toDTO(ticketTypeRepository.save(existing));
    }

    /**
     * Delete TicketType
     */
    @Transactional
    public void deleteTicketType(String companyId, String ticketTypeId) {
        TicketType existing = ticketTypeRepository.findByPublicIdAndCompanyPublicId(ticketTypeId, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket type not found"));
        ticketTypeRepository.delete(existing);
    }

    /**
     * Ensure no duplicate names for ticket types in the same company
     */
    private void validateTicketTypeNameUniqueness(String companyId, String name) {
        boolean exists = ticketTypeRepository.existsByCompanyPublicIdAndNameIgnoreCase(companyId, name);
        if (exists) {
            throw new IllegalArgumentException("Ticket type with this name already exists for the company");
        }
    }
}
