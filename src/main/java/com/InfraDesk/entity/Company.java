package com.InfraDesk.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "companies",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"name"}),
                @UniqueConstraint(columnNames = {"domain"}),
                @UniqueConstraint(columnNames = {"contactEmail"})
        },
        indexes = {
                @Index(name = "idx_company_domain", columnList = "domain")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(nullable = false, unique = true, updatable = false, length = 50)
    private String publicId = generatePublicId();

    /** Public-facing / display name */
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /** Registered legal entity name */
    @Column(length = 150)
    private String legalName;

    @Column(length = 100)
    private String industry;

    @Column(length = 20, unique = true)
    private String gstNumber;

    @Column(nullable = false, unique = true, length = 100)
    private String contactEmail;

    @Column(length = 15)
    private String contactPhone;

    private String address;

    @Column(name = "logo_url", length = 1024)
    private String logoUrl;

    /** Primary Tenant Domain */
    @Column(nullable = false, unique = true, length = 100)
    private String domain;

//    /** Company Domains */
    @Builder.Default
    @OneToMany(mappedBy = "company", cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JsonManagedReference("company-domains")
    private Set<CompanyDomain> domains = new HashSet<>();

    /** Subsidiaries */
    @Builder.Default
    @OneToMany(mappedBy = "parentCompany", cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JsonManagedReference("parent-subsidiaries")
    private Set<Company> subsidiaries = new HashSet<>();

    /** Parent Company */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_company_id")
    @JsonBackReference("parent-subsidiaries")
    @ToString.Exclude
    private Company parentCompany;

    /** Current Subscription */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_subscription_id")
    private Subscription currentSubscription;

    /** These below ros helps to delete the company hassle-free */

    @OneToMany(mappedBy = "company")
    private Set<Employee> employees = new HashSet<>();

    @OneToMany(mappedBy = "company")
    private Set<Membership> memberships = new HashSet<>();

    @Column(length = 20)
    private String shortCode;

    // Used to generate per-company asset tags safely; increment under DB lock
    @Builder.Default
    @Column(name = "asset_sequence", nullable = false)
    private Long assetSequence = 1L;

    /** Auditing */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    private String createdBy;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @LastModifiedBy
    private String updatedBy;

    /** Soft delete and active flags */
    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Boolean isDeleted = false;

    private static String generatePublicId() {
        return "COM-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }

        /**
         * Soft delete this company and all related entities to deactivate comprehensively.
         */
        public void softDelete() {
            this.isDeleted = true;
            this.isActive = false;

            // Soft delete subsidiaries recursively
            if (subsidiaries != null) {
                subsidiaries.forEach(Company::softDelete);
            }

            // Soft delete employees
            if (employees != null) {
                employees.forEach(emp -> {
                    emp.setIsDeleted(true);
                    emp.setIsActive(false);
                });
            }

            // Soft delete memberships
            if (memberships != null) {
                memberships.forEach(m -> m.setIsDeleted(true));
                memberships.forEach(m -> m.setIsActive(false));
            }
        }

        /**
         * Reactivate this company and all related entities recursively.
         */
        public void activate() {
            this.isActive = true;
            this.isDeleted = false;

            if (subsidiaries != null) {
                subsidiaries.forEach(Company::activate);
            }

            if (employees != null) {
                employees.forEach(emp -> {
                    emp.setIsActive(true);
                    emp.setIsDeleted(false);
                });
            }

            if (memberships != null) {
                memberships.forEach(m -> m.setIsDeleted(false));
                memberships.forEach(m -> m.setIsActive(true));
            }
        }

    /** Deactivate */
    public void deactivate() {
        this.isActive = false;
        this.isDeleted = false;

        if (subsidiaries != null) subsidiaries.forEach(Company::deactivate);
        if (employees != null) employees.forEach(emp -> {
            emp.setIsActive(false);
            emp.setIsDeleted(false);
        });
        if (memberships != null) memberships.forEach(m -> {
            m.setIsActive(false);
            m.setIsDeleted(false);
        });

    }
}
