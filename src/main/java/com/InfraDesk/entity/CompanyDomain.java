



package com.InfraDesk.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Table(
        name = "company_domains",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"domain"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Public unique identifier exposed to clients.
     * Generated automatically on persist if not set.
     */
    @Column(name = "public_id", nullable = false, unique = true, updatable = false, length = 36)
    private String publicId;

    /** Extra / secondary domain (must be unique) */
    @Column(nullable = false, unique = true, length = 100)
    private String domain;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    @ToString.Exclude // avoids potential LazyInitializationException in toString
    private Company company;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Boolean isDeleted = false;

    /**
     * Called before persisting to ensure publicId is set as a UUID string.
     */
    @PrePersist
    public void ensurePublicId() {
        if (publicId == null || publicId.isBlank()) {
            publicId = UUID.randomUUID().toString();
        }
    }

    /**
     * Soft delete this company domain (does NOT cascade)
     */
    public void softDelete() {
        this.isDeleted = true;
        this.isActive = false;
    }

    /**
     * Reactivate this company domain (does NOT cascade)
     */
    public void activate() {
        this.isActive = true;
        this.isDeleted = false;
    }
}
