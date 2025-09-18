package com.InfraDesk.entity;

import com.InfraDesk.enums.TicketAssigneeType;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
//
//@Entity
//@Table(name = "assignment_rules", indexes = {
//        @Index(name = "idx_rule_company_priority", columnList = "company_id, priority")
//})
//@Data @NoArgsConstructor @AllArgsConstructor @Builder
//public class TicketAssignmentRule {
//    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne(optional = false, fetch = FetchType.LAZY)
//    @JoinColumn(name = "company_id")
//    private Company company;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "ticket_type_id")
//    private TicketType ticketType;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "location_id")
//    private Location location;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "department_id")
//    private Department department;
//
//    // assignment target (one of these fields used)
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "assignee_user_id")
//    private User assigneeUser;
//
//    @Column(name = "assignee_role", length = 100)
//    private String assigneeRole;
//
//    @Column(name = "assignee_group", length = 100)
//    private String assigneeGroup;
//
//    @Column(name = "priority", nullable = false)
//    private Integer priority = 0; // higher = earlier
//}
//



@Entity
@Table(name = "assignment_rules", indexes = {
        @Index(name = "idx_rule_company_scope", columnList = "company_id, department_id, location_id, ticket_type_id"),
        @Index(name = "idx_rule_priority", columnList = "priority DESC")
})
@Data
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TicketAssignmentRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    private TicketType ticketType; // optional

    @ManyToMany
    @JoinTable(
            name = "rule_users",
            joinColumns = @JoinColumn(name = "rule_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> assigneeUsers = new HashSet<>(); // optional multiple users

    @ManyToMany
    @JoinTable(
            name = "rule_groups",
            joinColumns = @JoinColumn(name = "rule_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    private Set<Group> assigneeGroups = new HashSet<>(); // optional multiple groups

    @Column(name = "assignee_role", length = 100)
    private String assigneeRole; // optional role-based assignment

    @Column(nullable = false)
    private Integer priority = 0;
}

