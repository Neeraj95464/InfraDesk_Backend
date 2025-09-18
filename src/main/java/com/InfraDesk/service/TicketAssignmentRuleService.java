

package com.InfraDesk.service;

import com.InfraDesk.dto.TicketAssignmentRuleDTO;
import com.InfraDesk.entity.*;
import com.InfraDesk.mapper.TicketAssignmentRuleMapper;
import com.InfraDesk.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketAssignmentRuleService {

    private final TicketAssignmentRuleRepository ruleRepository;
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final LocationRepository locationRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    /**
     * Create or update a rule for a company using DTO
     */
    public TicketAssignmentRuleDTO saveRule(String companyId, TicketAssignmentRuleDTO dto) {
//        System.out.println("Request was "+dto);
        Company company = companyRepository.findByPublicId(companyId)
                .orElseThrow(() -> new EntityNotFoundException("Company not found"));

        Department department = departmentRepository.findByPublicIdAndCompany_PublicId(dto.getDepartmentId(),companyId)
                .orElseThrow(() -> new EntityNotFoundException("Department not found"));

//        Location location = locationRepository.findByPublicIdAndCompany_PublicId(dto.getLocationId(),companyId)
//                .orElseThrow(() -> new EntityNotFoundException("Location not found"));
        Location location = null;
        if (dto.getLocationId() != null) {
            location = locationRepository.findByPublicIdAndCompany_PublicId(dto.getLocationId(), companyId)
                    .orElseThrow(() -> new EntityNotFoundException("Location not found"));
        }

        TicketType ticketType = null;
        if (dto.getTicketTypeId() != null) {
            ticketType = ticketTypeRepository.findByPublicIdAndCompanyPublicId(dto.getTicketTypeId(),companyId)
                    .orElseThrow(() -> new EntityNotFoundException("TicketType not found"));
        }

        Set<User> users = (dto.getAssigneeUserIds() != null)
                ? userRepository.findByPublicIdIn(dto.getAssigneeUserIds()).stream().collect(Collectors.toSet())
                : Set.of();

        Set<Group> groups = (dto.getAssigneeGroupIds() != null && !dto.getAssigneeGroupIds().isEmpty())
                ? new HashSet<>(groupRepository.findByPublicIdIn(dto.getAssigneeGroupIds()))   // <-- always collects to new set
                : Set.of();

        TicketAssignmentRule rule = TicketAssignmentRuleMapper.toEntity(dto, company, department, location, ticketType, users, groups);
        TicketAssignmentRule savedRule = ruleRepository.save(rule);

        return TicketAssignmentRuleMapper.toDTO(savedRule);
    }

    /**
     * Get a rule by ID and company
     */
    @Transactional(readOnly = true)
    public TicketAssignmentRuleDTO getRule(String companyId, Long ruleId) {
        TicketAssignmentRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new EntityNotFoundException("Rule not found"));

        if (!rule.getCompany().getPublicId().equals(companyId)) {
            throw new IllegalArgumentException("Rule does not belong to this company");
        }

        return TicketAssignmentRuleMapper.toDTO(rule);
    }

    /**
     * Get all rules for a company ordered by priority
     */
    @Transactional(readOnly = true)
    public List<TicketAssignmentRuleDTO> getRulesByCompany(String companyId) {
        Company company = companyRepository.findByPublicId(companyId)
                .orElseThrow(() -> new EntityNotFoundException("Company not found"));

        return ruleRepository.findByCompanyOrderByPriorityDesc(company)
                .stream()
                .map(TicketAssignmentRuleMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long countRulesByCompany(String companyId) {
        Company company = companyRepository.findByPublicId(companyId)
                .orElseThrow(() -> new EntityNotFoundException("Company not found"));
        return ruleRepository.countByCompany(company);
    }


    /**
     * Delete a rule by ID
     */
    public void deleteRule(String companyId, Long ruleId) {
        TicketAssignmentRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new EntityNotFoundException("Rule not found"));

        if (!rule.getCompany().getPublicId().equals(companyId)) {
            throw new IllegalArgumentException("Rule does not belong to this company");
        }

        ruleRepository.delete(rule);
    }
}
