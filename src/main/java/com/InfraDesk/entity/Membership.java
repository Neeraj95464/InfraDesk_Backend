package com.InfraDesk.entity;

import com.InfraDesk.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "memberships",
        uniqueConstraints = {
                @UniqueConstraint(name = "uc_user_company", columnNames = {"user_id", "company_id"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 50)
    private String publicId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false, updatable = false)
    private Company company;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role;

    @CreatedBy
    @Column(name = "created_by", length = 100, updatable = false)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Boolean isDeleted = false;

//    private static String generatePublicId() {
//        return "MEM-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
//    }

    private static final String PUBLIC_ID_PREFIX = "MEM-";
    private static final int PUBLIC_ID_LENGTH = 12;

    @PrePersist
    protected void onCreate() {
        if (publicId == null || publicId.isEmpty()) {
            publicId = PUBLIC_ID_PREFIX + UUID.randomUUID().toString().replace("-", "").substring(0, PUBLIC_ID_LENGTH).toUpperCase();
        }
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (isDeleted == null) {
            isDeleted = false;
        }
    }
}
