package com.InfraDesk.service;

import com.InfraDesk.entity.*;
import com.InfraDesk.enums.Role;
import com.InfraDesk.enums.TicketStatus;
import com.InfraDesk.enums.TicketAssigneeType;
import com.InfraDesk.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class TicketAssignmentService {

    private final TicketAssignmentRuleRepository ruleRepository;
    private final TicketRepository ticketRepository;

    public TicketAssignmentService(TicketAssignmentRuleRepository ruleRepository,
                                   TicketRepository ticketRepository) {
        this.ruleRepository = ruleRepository;
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    public void assignTicket(Ticket ticket) {
        List<TicketAssignmentRule> rules = null;

        if (ticket.getLocation() == null) {
            rules = ruleRepository.findByCompanyIdAndDepartmentIdAndLocationIsNullOrderByPriorityDesc(
                    ticket.getCompany().getId(),
                    ticket.getDepartment().getId()
            );
        } else {
            rules = ruleRepository.findByCompanyIdAndDepartmentIdAndLocationIdOrderByPriorityDesc(
                    ticket.getCompany().getId(),
                    ticket.getDepartment().getId(),
                    ticket.getLocation().getId()
            );
        }

        // pick first matching rule
        for (TicketAssignmentRule rule : rules) {
            if (rule.getTicketType() != null &&
                    !rule.getTicketType().getId().equals(ticket.getTicketType().getId())) {
                continue; // skip if ticketType doesn't match
            }

            // 1️⃣ Assign to specific users
            if (!rule.getAssigneeUsers().isEmpty()) {
                User user = pickLeastBusyUser(rule.getAssigneeUsers());
                ticket.setAssignee(user);
                break;
            }

            // 2️⃣ Assign to groups
            if (!rule.getAssigneeGroups().isEmpty()) {
                Set<User> groupUsers = rule.getAssigneeGroups().stream()
                        .flatMap(g -> g.getUsers().stream())
                        .collect(Collectors.toSet());
                User user = pickLeastBusyUser(groupUsers);
                ticket.setAssignee(user);
                break;
            }

            // 3️⃣ Assign by role
            if (rule.getAssigneeRole() != null) {
                User user = pickLeastBusyUserByRole(ticket.getCompany(), rule.getAssigneeRole());
                ticket.setAssignee(user);
                break;
            }
        }

        ticketRepository.save(ticket);
    }

    private User pickLeastBusyUser(Set<User> users) {
        return users.stream()
                .min(Comparator.comparingLong(u -> ticketRepository.countByAssigneeAndStatusIn(
                        u, List.of(TicketStatus.OPEN, TicketStatus.WAITING)
                )))
                .orElse(null);
    }

    private User pickLeastBusyUserByRole(Company company, String roleName) {
        // implement fetching users by role and choose least busy
        // similar to pickLeastBusyUser logic
        return null; // implement as needed
    }
}
