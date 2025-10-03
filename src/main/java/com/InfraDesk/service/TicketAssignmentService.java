//package com.InfraDesk.service;
//
//import com.InfraDesk.entity.*;
//import com.InfraDesk.enums.Role;
//import com.InfraDesk.enums.TicketStatus;
//import com.InfraDesk.repository.*;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Comparator;
//import java.util.List;
//import java.util.Optional;
//
//@Service
//public class TicketAssignmentService {
//    private final TicketAssignmentRuleRepository ruleRepository;
//    private final UserRepository userRepository;
//    private final MembershipRepository membershipRepository;
//    private final TicketRepository ticketRepository;
//
//    public TicketAssignmentService(TicketAssignmentRuleRepository ruleRepository,
//                                   UserRepository userRepository, MembershipRepository membershipRepository,
//                                   TicketRepository ticketRepository) {
//        this.ruleRepository = ruleRepository;
//        this.userRepository = userRepository;
//        this.membershipRepository = membershipRepository;
//        this.ticketRepository = ticketRepository;
//    }
//
//    /**
//     * Resolve assignment for a freshly created ticket.
//     * Simple algorithm: load rules for company -> filter matching conditions -> choose highest priority rule.
//     * If rule has assigneeUser -> assign
//     * If rule has assigneeRole -> choose least-busy user with that role
//     * If none -> leave unassigned
//     */
//    @Transactional
//    public void assign(Ticket ticket) {
//        List<TicketAssignmentRule> rules = ruleRepository.findByCompanyIdOrderByPriorityDesc(ticket.getCompany().getId());
//        Optional<TicketAssignmentRule> match = rules.stream()
//                .filter(r -> matches(r, ticket))
//                .sorted(Comparator.comparing(TicketAssignmentRule::getPriority).reversed())
//                .findFirst();
//
//        if (match.isEmpty()) return;
//
//        TicketAssignmentRule rule = match.get();
//
//        if (rule.getAssigneeUser() != null) {
//            ticket.setAssignee(rule.getAssigneeUser());
//        } else if (rule.getAssigneeRole() != null) {
//            // naive least-busy: pick user with role and fewest open tickets
//            User chosen = pickLeastBusyUserWithRole(ticket.getCompany().getId(), rule.getAssigneeRole());
//            ticket.setAssignee(chosen);
//        }
//        ticketRepository.save(ticket);
//    }
//
//    private boolean matches(TicketAssignmentRule r, Ticket t) {
//        if (r.getTicketType() != null && !r.getTicketType().getId().equals(t.getTicketType().getId())) return false;
//        if (r.getLocation() != null && (t.getLocation() == null || !r.getLocation().getId().equals(t.getLocation().getId()))) return false;
//        if (r.getDepartment() != null && (t.getDepartment() == null || !r.getDepartment().getId().equals(t.getDepartment().getId()))) return false;
//        return true;
//    }
//
//
//
//    private User pickLeastBusyUserWithRole(Long companyId, String roleName) {
//        List<Membership> memberships = membershipRepository.findByCompanyIdAndRoleAndIsActiveTrue(companyId, Role.valueOf(roleName));
//        if (memberships.isEmpty()) return null;
//
//        User bestUser = null;
//        long minOpen = Long.MAX_VALUE;
//
//        for (Membership membership : memberships) {
//            User user = membership.getUser();
//            List<TicketStatus> openStatuses = List.of(TicketStatus.OPEN, TicketStatus.WAITING); // example statuses
//            long openTicketCount = ticketRepository.countByAssignee_IdAndStatusIn(user.getId(), openStatuses);
//
////            long openCount = ticketRepository.countByAssigneeIdAndStatusIn(user.getId(), List.of(/* open statuses */));
//            if (openTicketCount < minOpen) {
//                minOpen = openTicketCount;
//                bestUser = user;
//            }
//        }
//        return bestUser;
//    }
//
//
//
//}
//




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

//@Service
//public class TicketAssignmentService {
//
//    private final TicketAssignmentRuleRepository ruleRepository;
//    private final UserRepository userRepository;
//    private final MembershipRepository membershipRepository;
//    private final GroupRepository groupRepository;
//    private final TicketRepository ticketRepository;
//
//    public TicketAssignmentService(TicketAssignmentRuleRepository ruleRepository,
//                                   UserRepository userRepository,
//                                   MembershipRepository membershipRepository,
//                                   GroupRepository groupRepository,
//                                   TicketRepository ticketRepository) {
//        this.ruleRepository = ruleRepository;
//        this.userRepository = userRepository;
//        this.membershipRepository = membershipRepository;
//        this.groupRepository = groupRepository;
//        this.ticketRepository = ticketRepository;
//    }
//
//    /**
//     * Assign a ticket automatically based on rules.
//     */
//    @Transactional
//    public void assign(Ticket ticket) {
//
//        List<TicketAssignmentRule> rules = ruleRepository
//                .findByCompanyIdOrderByPriorityDesc(ticket.getCompany().getId());
//
//        Optional<TicketAssignmentRule> match = rules.stream()
//                .filter(r -> matches(r, ticket))
//                .findFirst();
//
//        if (match.isEmpty()) return;
//
//        TicketAssignmentRule rule = match.get();
//
//        switch (rule.getAssigneeType()) {
//            case USER -> {
//                // assigneeRef contains User ID
//                Long userId = Long.parseLong(rule.getAssigneeRef());
//                userRepository.findById(userId).ifPresent(ticket::setAssignee);
//            }
//            case ROLE -> {
//                Role role = Role.valueOf(rule.getAssigneeRef());
//                User chosen = pickLeastBusyUserWithRole(ticket.getCompany().getId(), role);
//                ticket.setAssignee(chosen);
//            }
//            case GROUP -> {
//                // TODO: implement group assignment strategy
//                Group group = groupRepository.findByName(rule.getAssigneeRef()).orElse(null);
//                User chosen = pickLeastBusyUserFromGroup(group, ticket);
//                ticket.setAssignee(chosen);
//            }
//        }
//
//        ticketRepository.save(ticket);
//    }
//
//    /**
//     * Check if a rule matches the ticket.
//     */
//    private boolean matches(TicketAssignmentRule rule, Ticket ticket) {
//        // Mandatory location & department
//        if (!rule.getLocation().getId().equals(ticket.getLocation().getId())) return false;
//        if (!rule.getDepartment().getId().equals(ticket.getDepartment().getId())) return false;
//
//        // Optional ticket type
//        if (rule.getTicketType() != null) {
//            if (ticket.getTicketType() == null || !rule.getTicketType().getId().equals(ticket.getTicketType().getId())) {
//                return false;
//            }
//        }
//
//        return true;
//    }
//
//    /**
//     * Pick least busy user with a specific role in the company.
//     */
//    private User pickLeastBusyUserWithRole(Long companyId, Role role) {
//        List<Membership> memberships = membershipRepository.findByCompanyIdAndRoleAndIsActiveTrue(companyId, role);
//        if (memberships.isEmpty()) return null;
//
//        User bestUser = null;
//        long minOpen = Long.MAX_VALUE;
//
//        for (Membership membership : memberships) {
//            User user = membership.getUser();
//            long openCount = ticketRepository.countByAssignee_IdAndStatusIn(
//                    user.getId(),
//                    List.of(TicketStatus.OPEN, TicketStatus.WAITING)
//            );
//            if (openCount < minOpen) {
//                minOpen = openCount;
//                bestUser = user;
//            }
//        }
//        return bestUser;
//    }
//
//    /**
//     * Pick least busy user from a group
//     */
//    private User pickLeastBusyUserFromGroup(Group group, Ticket ticket) {
//        if (group == null || group.getUsers().isEmpty()) return null;
//
//        User bestUser = null;
//        long minOpen = Long.MAX_VALUE;
//
//        for (User user : group.getUsers()) {
//            long openCount = ticketRepository.countByAssignee_IdAndStatusIn(
//                    user.getId(),
//                    List.of(TicketStatus.OPEN, TicketStatus.WAITING)
//            );
//            if (openCount < minOpen) {
//                minOpen = openCount;
//                bestUser = user;
//            }
//        }
//        return bestUser;
//    }
//}



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
