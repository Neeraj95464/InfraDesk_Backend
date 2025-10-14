package com.InfraDesk.mapper;

import com.InfraDesk.dto.AssetResponseDTO;
import com.InfraDesk.entity.Asset;
import com.InfraDesk.enums.AssetStatus;
import com.InfraDesk.enums.AssetType;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class AssetMapper {
    public AssetResponseDTO toDto(Asset asset) {
        if (asset == null) return null;

        return AssetResponseDTO.builder()
                .publicId(asset.getPublicId())
                .name(asset.getName())
                .assetTag(asset.getAssetTag())
                .description(asset.getDescription())
                .status(asset.getStatus() != null ? AssetStatus.valueOf(asset.getStatus().name()) : null)
                .assetType(asset.getAssetType() != null ? AssetType.valueOf(asset.getAssetType().name()) : null)
                .locationId(asset.getLocation() != null ? asset.getLocation().getPublicId() : null)
                .siteId(asset.getSite() != null ? asset.getSite().getPublicId() : null)
                .assigneePublicId(asset.getAssignee() != null ? asset.getAssignee().getPublicId() : null)
//                .peripherals(asset.getPeripherals().stream().map(p -> p.getName()).collect(Collectors.toList()))
                .mediaUrls(asset.getMedia().stream().map(m -> m.getFileUrl()).collect(Collectors.toList()))
                .purchaseDate(asset.getPurchaseDate())
                .warrantyUntil(asset.getWarrantyUntil())
                .isAssignedToLocation(asset.getIsAssignedToLocation())
                .build();
    }

}

