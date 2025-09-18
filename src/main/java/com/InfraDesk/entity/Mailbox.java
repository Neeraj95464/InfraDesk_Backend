package com.InfraDesk.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mailboxes", uniqueConstraints = {
        @UniqueConstraint(name = "uc_mailbox_address", columnNames = {"email_address"})
})
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Mailbox {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email_address", nullable = false, length = 255)
    private String emailAddress;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_ticket_type_id")
    private TicketType defaultTicketType;

    @Column(name = "display_name")
    private String displayName;
}
