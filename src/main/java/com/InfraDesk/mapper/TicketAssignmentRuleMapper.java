//package com.InfraDesk.mapper;
//
//import com.InfraDesk.dto.TicketAssignmentRuleDTO;
//import com.InfraDesk.entity.*;
//
//import java.util.Set;
//import java.util.stream.Collectors;
//
//public class TicketAssignmentRuleMapper {
//
//    private TicketAssignmentRuleMapper() {
//        // utility class
//    }
//
//    public static TicketAssignmentRuleDTO toDTO(TicketAssignmentRule entity) {
//        if (entity == null) return null;
//
//        return TicketAssignmentRuleDTO.builder()
//                .id(entity.getId())
//                .companyId(entity.getCompany() != null ? entity.getCompany().getPublicId() : null)
//                .companyName(entity.getCompany() != null ? entity.getCompany().getName() : null)
//                .departmentId(entity.getDepartment() != null ? entity.getDepartment().getPublicId() : null)
//                .departmentName(entity.getDepartment() != null ? entity.getDepartment().getName() : null)
//                .locationId(entity.getLocation() != null ? entity.getLocation().getPublicId() : null)
//                .locationName(entity.getLocation() != null ? entity.getLocation().getName() : null)
//                .ticketTypeId(entity.getTicketType() != null ? entity.getTicketType().getId() : null)
//                .ticketTypeName(entity.getTicketType() != null ? entity.getTicketType().getName() : null)
//                .assigneeUserIds(entity.getAssigneeUsers().stream().map(User::getPublicId).collect(Collectors.toSet()))
//                .assigneeUserNames(entity.getAssigneeUsers().stream().map(User::getEmail).collect(Collectors.toSet()))
//                .assigneeGroupIds(entity.getAssigneeGroups().stream()
//                        .map(Group::getPublicId)
//                        .collect(Collectors.toSet()))
//                .assigneeGroupNames(entity.getAssigneeGroups().stream().map(Group::getName).collect(Collectors.toSet()))
//                .assigneeRole(entity.getAssigneeRole())
//                .priority(entity.getPriority())
//                .build();
//    }
//
//    public static TicketAssignmentRule toEntity(TicketAssignmentRuleDTO dto,
//                                                Company company,
//                                                Department department,
//                                                Location location,
//                                                TicketType ticketType,
//                                                Set<User> assigneeUsers,
//                                                Set<Group> assigneeGroups) {
//        if (dto == null) return null;
//
//        return TicketAssignmentRule.builder()
//                .id(dto.getId())
//                .company(company)
//                .department(department)
//                .location(location)
//                .ticketType(ticketType)
//                .assigneeUsers(assigneeUsers)
//                .assigneeGroups(assigneeGroups)
//                .assigneeRole(dto.getAssigneeRole())
//                .priority(dto.getPriority() != null ? dto.getPriority() : 0)
//                .build();
//    }
//}




package com.InfraDesk.mapper;

import com.InfraDesk.dto.TicketAssignmentRuleDTO;
import com.InfraDesk.entity.*;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class TicketAssignmentRuleMapper {

    private TicketAssignmentRuleMapper() {
        // utility class
    }

    public static TicketAssignmentRuleDTO toDTO(TicketAssignmentRule entity) {
        if (entity == null) return null;

        return TicketAssignmentRuleDTO.builder()
                .id(entity.getId())
                .companyId(entity.getCompany() != null ? entity.getCompany().getPublicId() : null)
                .companyName(entity.getCompany() != null ? entity.getCompany().getName() : null)
                .departmentId(entity.getDepartment() != null ? entity.getDepartment().getPublicId() : null)
                .departmentName(entity.getDepartment() != null ? entity.getDepartment().getName() : null)
                .locationId(entity.getLocation() != null ? entity.getLocation().getPublicId() : null)
                .locationName(entity.getLocation() != null ? entity.getLocation().getName() : null)
                .ticketTypeId(entity.getTicketType() != null ? entity.getTicketType().getPublicId() : null)
                .ticketTypeName(entity.getTicketType() != null ? entity.getTicketType().getName() : null)
                .assigneeUserIds(entity.getAssigneeUsers() != null
                        ? entity.getAssigneeUsers().stream().map(User::getPublicId).collect(Collectors.toSet())
                        : Set.of())
                .assigneeUserNames(entity.getAssigneeUsers() != null
                        ? entity.getAssigneeUsers().stream().map(User::getEmail).collect(Collectors.toSet())
                        : Set.of())
                .assigneeGroupIds(entity.getAssigneeGroups() != null
                        ? entity.getAssigneeGroups().stream().map(Group::getPublicId).collect(Collectors.toSet())
                        : Set.of())
                .assigneeGroupNames(entity.getAssigneeGroups() != null
                        ? entity.getAssigneeGroups().stream().map(Group::getName).collect(Collectors.toSet())
                        : Set.of())
                .assigneeRole(entity.getAssigneeRole())
                .priority(entity.getPriority())
                .build();
    }

    public static TicketAssignmentRule toEntity(TicketAssignmentRuleDTO dto,
                                                Company company,
                                                Department department,
                                                Location location,
                                                TicketType ticketType,
                                                Set<User> assigneeUsers,
                                                Set<Group> assigneeGroups) {
        if (dto == null) return null;

        return TicketAssignmentRule.builder()
                .id(dto.getId())
                .company(company)
                .department(department)
                .location(location)
                .ticketType(ticketType)
                // ✅ wrap in new HashSet to avoid ConcurrentModificationException
                .assigneeUsers(assigneeUsers != null ? new HashSet<>(assigneeUsers) : new HashSet<>())
                .assigneeGroups(assigneeGroups != null ? new HashSet<>(assigneeGroups) : new HashSet<>())
                .assigneeRole(dto.getAssigneeRole())
                .priority(dto.getPriority() != null ? dto.getPriority() : 0)
                .build();
    }
}
