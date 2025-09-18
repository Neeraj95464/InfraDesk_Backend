//package com.InfraDesk.entity;
//
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.time.LocalDateTime;
////
////
////@Entity
////@Table(name = "site_location_assignment", uniqueConstraints = {
////        @UniqueConstraint(columnNames = {"site_id", "location_id"})
////})
////@Data
////@NoArgsConstructor
////@AllArgsConstructor
////@Builder
////public class SiteLocationAssignment {
////
////    @Id
////    @GeneratedValue(strategy = GenerationType.IDENTITY)
////    private Long id;
////
////    @ManyToOne(fetch = FetchType.LAZY)
////    @JoinColumn(name = "site_id", nullable = false)
////    private Site site;
////
////    @ManyToOne(fetch = FetchType.LAZY)
////    @JoinColumn(name = "location_id", nullable = false)
////    private Location location;
////
////    private Boolean isActive = true;
////
////    private String createdBy;
////
////    @Column(nullable = false, updatable = false)
////    private LocalDateTime createdAt = LocalDateTime.now();
////
////    @Column(nullable = false)
////    private Boolean isDeleted = false;
////}
////
//
//
//
////package com.infradesk.entity;
//
//import org.hibernate.annotations.SQLDelete;
//import org.hibernate.annotations.Where;
//import org.springframework.data.annotation.CreatedBy;
//import org.springframework.data.annotation.CreatedDate;
//import org.springframework.data.jpa.domain.support.AuditingEntityListener;
//
//
//@Entity
//@Table(
//        name = "site_location_assignments",
//        uniqueConstraints = {
//                @UniqueConstraint(name = "uq_site_location_company", columnNames = {"site_id", "location_id", "company_id"})
//        },
//        indexes = {
//                @Index(name = "idx_assignment_site", columnList = "site_id"),
//                @Index(name = "idx_assignment_location", columnList = "location_id"),
//                @Index(name = "idx_assignment_company", columnList = "company_id")
//        }
//)
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//@EntityListeners(AuditingEntityListener.class)
//@SQLDelete(sql = "UPDATE site_location_assignments SET is_deleted = true WHERE id = ?")
//@Where(clause = "is_deleted = false")
//public class SiteLocationAssignment {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    // Tenant scope
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "company_id", nullable = false)
//    private Company company;
//
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "site_id", nullable = false)
//    private Site site;
//
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "location_id", nullable = false)
//    private Location location;
//
//    @Column(name = "is_active", nullable = false, columnDefinition = "boolean default true")
//    private Boolean isActive = true;
//
//    @CreatedBy
//    @Column(name = "created_by", length = 100, updatable = false)
//    private String createdBy;
//
//    @CreatedDate
//    @Column(name = "created_at", nullable = false, updatable = false)
//    private LocalDateTime createdAt;
//
//    @Column(name = "is_deleted", nullable = false, columnDefinition = "boolean default false")
//    private Boolean isDeleted = false;
//}


package com.InfraDesk.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "site_location_assignments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_site_location_company", columnNames = {"site_id", "location_id", "company_id"})
        },
        indexes = {
                @Index(name = "idx_assignment_site", columnList = "site_id"),
                @Index(name = "idx_assignment_location", columnList = "location_id"),
                @Index(name = "idx_assignment_company", columnList = "company_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE site_location_assignments SET is_deleted = true WHERE id = ?")
public class SiteLocationAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @Column(name = "public_id", nullable = false, unique = true, updatable = false, length = 36)
//    private String publicId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "is_active", nullable = false, columnDefinition = "boolean default true")
    private Boolean isActive = true;

    @CreatedBy
    @Column(name = "created_by", length = 100, updatable = false)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "boolean default false")
    private Boolean isDeleted = false;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
//        publicId = (publicId == null) ? UUID.randomUUID().toString() : publicId;
        isActive = (isActive == null) ? Boolean.TRUE : isActive;
        isDeleted = (isDeleted == null) ? Boolean.FALSE : isDeleted;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public static class AuditListener {
        @PrePersist
        public void setCreatedAtAndPublicId(Location loc) {
            loc.prePersist();
        }

        @PreUpdate
        public void setUpdatedAt(Location loc) {
            loc.preUpdate();
        }
    }
}
