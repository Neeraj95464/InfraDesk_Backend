package com.InfraDesk.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

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

