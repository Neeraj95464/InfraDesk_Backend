package com.InfraDesk.entity;

import com.InfraDesk.enums.MediaType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "asset_media",
        indexes = {
                @Index(name = "idx_asset_media_asset", columnList = "asset_id"),
                @Index(name = "idx_asset_media_type", columnList = "mediaType")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AssetMedia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(name = "public_id", nullable = false, updatable = false, unique = true)
    private String publicId = java.util.UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String fileUrl; // where the file is stored (S3 or local path)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MediaType mediaType;

    private LocalDateTime uploadedAt = LocalDateTime.now();
    private String uploadedBy;

    @PrePersist
    public void prePersist() {
        if (uploadedAt == null) {
            uploadedAt = LocalDateTime.now();
        }
    }

}

