package com.InfraDesk.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
//
//@Entity
//@Table(name = "sites")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class Site {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "company_id", nullable = false)
//    private Company company;
//
//    @Column(nullable = false, length = 100)
//    private String name; // e.g., "Bangalore HQ", "Delhi Office"
//
//    @Column(length = 255)
//    private String address;
//
//    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Location> locations;
//
//    private Boolean isActive = true;
//
//    private String createdBy;
//
//    @Column(nullable = false, updatable = false)
//    private LocalDateTime createdAt = LocalDateTime.now();
//
//    @Column(nullable = false)
//    private Boolean isDeleted = false;
//
//}
//

//
//package com.InfraDesk.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//import org.hibernate.annotations.SQLDelete;
//import org.hibernate.annotations.Where;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "sites",
        uniqueConstraints = @UniqueConstraint(columnNames = {"name", "company_id"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE sites SET is_deleted = true WHERE id = ?")
@EntityListeners(Site.AuditListener.class)
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Immutable public identifier for API exposure
    @Column(name = "public_id", nullable = false, unique = true, updatable = false, length = 36)
    private String publicId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String address;

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Location> locations = new ArrayList<>();

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

    @Version
    private Long version; // Optimistic locking

    public static class AuditListener {
        @PrePersist
        public void prePersist(Site entity) {
            entity.createdAt = LocalDateTime.now();
            entity.isActive = (entity.isActive == null) ? Boolean.TRUE : entity.isActive;
            entity.isDeleted = (entity.isDeleted == null) ? Boolean.FALSE : entity.isDeleted;
            entity.publicId = entity.publicId == null
                    ? UUID.randomUUID().toString()
                    : entity.publicId;
        }

        @PreUpdate
        public void preUpdate(Site entity) {
            entity.updatedAt = LocalDateTime.now();
        }
    }
}

