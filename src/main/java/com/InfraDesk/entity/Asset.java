package com.InfraDesk.entity;

import com.InfraDesk.enums.AssetStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "assets",
        uniqueConstraints = {
                @UniqueConstraint(name = "uc_asset_tag_company", columnNames = {"asset_tag", "company_id"}),
                @UniqueConstraint(name = "uc_asset_public_id", columnNames = {"public_id"})
        },
        indexes = {
                @Index(name = "idx_asset_company", columnList = "company_id"),
                @Index(name = "idx_asset_tag", columnList = "asset_tag")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE assets SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, updatable = false, unique = true, length = 50)
    private String publicId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Column(name = "asset_tag", length = 100, nullable = false)
    private String assetTag;

    @Column(length = 1000)
    private String description;

    @Column(length = 500)
    private String note;

    private String brand;
    private String model;
    private BigDecimal cost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id")
    private Site site;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    private LocalDate reservationStartDate;
    private LocalDate reservationEndDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_type_id",nullable = false)
    private CompanyAssetType assetType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private AssetStatus status = AssetStatus.AVAILABLE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_asset_id")
    private Asset parentAsset;

    @OneToMany(mappedBy = "parentAsset", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AssetPeripheral> peripherals = new HashSet<>();

    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AssetMedia> media = new ArrayList<>();

    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AssetHistory> history = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_employee_id")
    private Employee assignee;

    private Boolean isAssignedToLocation = false;

    private LocalDate purchaseDate;
    private String purchasedFrom;
    private LocalDate warrantyUntil;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(length = 100)
    private String createdBy;

    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Boolean isDeleted = false;

//    @PrePersist
//    protected void onCreate() {
//        if (publicId == null)
//            publicId = "AST-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
//        createdAt = LocalDateTime.now();
//        updatedAt = createdAt;
//        isDeleted = false;
//    }

    private static final String PUBLIC_ID_PREFIX = "AST-";
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

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

