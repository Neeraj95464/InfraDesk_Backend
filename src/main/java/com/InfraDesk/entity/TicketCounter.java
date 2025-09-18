package com.InfraDesk.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ticket_counters", uniqueConstraints = {
        @UniqueConstraint(name = "uc_counter_company_department", columnNames = {"company_id","department_id"})
})
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TicketCounter {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    // department can be null (company-level counter)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "last_seq", nullable = false)
    private Long lastSeq;
}
