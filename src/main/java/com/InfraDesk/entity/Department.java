


package com.InfraDesk.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDateTime;

@Entity
@Table(name = "departments", uniqueConstraints = {
        @UniqueConstraint(name = "uc_department_company_name", columnNames = {"name", "company_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE departments SET is_deleted = true WHERE id = ?")
@EntityListeners(Department.AuditListener.class)
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Add publicId as UUID string
    @Column(name = "public_id", nullable = true, unique = true, updatable = false, length = 36)
    private String publicId;

    @Column(nullable = false, length = 50)
    private String name; // e.g., "IT", "HR", "Security"

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = Boolean.TRUE;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = Boolean.FALSE;

    @Column(name = "created_by", length = 100, updatable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Generate UUID on persist if not set
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        if (publicId == null || publicId.isBlank()) {
            publicId = java.util.UUID.randomUUID().toString();
        }
        // Optionally set createdBy here from security context
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
        // Optionally set updatedBy here from security context
    }

    public static class AuditListener {
        @PrePersist
        public void setCreatedAtAndPublicId(Department dept) {
            dept.prePersist();
        }

        @PreUpdate
        public void setUpdatedAt(Department dept) {
            dept.preUpdate();
        }
    }
}

