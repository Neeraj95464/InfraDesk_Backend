package com.InfraDesk.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "company_settings",
        uniqueConstraints = @UniqueConstraint(name = "uc_settings_company", columnNames = {"company_id"}),
        indexes = @Index(name = "idx_settings_company", columnList = "company_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE company_settings SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class CompanySettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false, unique = true, length = 50)
    private String companyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_company_id")
    private Company parentCompany;

    @Column(name = "ticket_default_due_days", nullable = false)
    private Integer ticketDefaultDueDays = 3;

    @Column(name = "company_short_code", nullable = false, length = 20)
    private String companyShortCode;

    @Column(name = "asset_tag_required", nullable = false)
    private Boolean assetTagRequired = false;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}


