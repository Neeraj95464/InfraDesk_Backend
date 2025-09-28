package com.InfraDesk.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ticketing_department_config",
        uniqueConstraints = @UniqueConstraint(columnNames = {"company_id", "department_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE ticketing_department_config SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
@EntityListeners(TicketingDepartmentConfig.AuditListener.class)
public class TicketingDepartmentConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, updatable = false, length = 50)
    private String publicId = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(nullable = false)
    private Boolean ticketEnabled = Boolean.TRUE;

    @Column(length = 150)
    private String ticketEmail;

    @Column(length = 500)
    private String note;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = Boolean.TRUE;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = Boolean.FALSE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "created_by", length = 100, updatable = false)
    private String createdBy;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @PrePersist
    public void prePersist() {
        if (publicId == null) {
            publicId = UUID.randomUUID().toString();
        }
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (isActive == null) isActive = Boolean.TRUE;
        if (isDeleted == null) isDeleted = Boolean.FALSE;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public static class AuditListener {
        @PrePersist
        public void prePersist(TicketingDepartmentConfig config) { config.prePersist(); }
        @PreUpdate
        public void preUpdate(TicketingDepartmentConfig config) { config.preUpdate(); }
    }
}
