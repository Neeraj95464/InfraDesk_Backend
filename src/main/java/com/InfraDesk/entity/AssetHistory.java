package com.InfraDesk.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "asset_history",
        indexes = {
                @Index(name = "idx_asset_history_asset", columnList = "asset_id"),
                @Index(name = "idx_asset_history_modified_at", columnList = "modifiedAt")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AssetHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(name = "field_name", nullable = false, length = 100)
    private String fieldName;

    @Column(length = 1000)
    private String oldValue;

    @Column(length = 1000)
    private String newValue;

    @Column(nullable = false)
    private String modifiedBy; // publicId of actor

    @Column(nullable = false)
    private LocalDateTime modifiedAt = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        if (modifiedAt == null) {
            modifiedAt = LocalDateTime.now();
        }
    }


}

