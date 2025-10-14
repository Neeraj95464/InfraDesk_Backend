package com.InfraDesk.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "asset_peripherals",
        indexes = {
                @Index(name = "idx_peripheral_parent", columnList = "parent_asset_id"),
                @Index(name = "idx_peripheral_serial", columnList = "serialNumber")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AssetPeripheral {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, updatable = false, unique = true)
    private String publicId = "PRF-" + UUID.randomUUID().toString().substring(0,12).toUpperCase();

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 120)
    private String serialNumber;

    @Column(length = 500)
    private String note;

    private LocalDate warrantyUntil;

    @Column(nullable = false)
    private Boolean isDeleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_asset_id", nullable = false)
    private Asset parentAsset;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = true, length = 100)
    private String createdBy;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

}
