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
                .brand(asset.getBrand())
                .cost(asset.getCost())
                .model(asset.getModel())

                .parentAssetId(asset.getParentAsset() != null ? asset.getParentAsset().getPublicId() : null)
                .assetTag(asset.getAssetTag())
                .description(asset.getDescription())
                .serialNumber(asset.getSerialNumber())
                .note(asset.getNote())
                .assigneeEmployeeId(asset.getAssignee() != null ? asset.getAssignee().getEmployeeId() : null)
                .assigneeEmployeeName(asset.getAssignee() != null ? asset.getAssignee().getName() : null)
                .status(asset.getStatus() != null ? AssetStatus.valueOf(asset.getStatus().name()) : null)
                .assetType(asset.getAssetType() != null ? AssetType.valueOf(asset.getAssetType().getTypeName()) : null)
                .locationId(asset.getLocation() != null ? asset.getLocation().getPublicId() : null)
                .siteId(asset.getSite() != null ? asset.getSite().getPublicId() : null)
                .siteName(asset.getSite() != null ? asset.getSite().getName() : null)
                .locationName(asset.getLocation() != null ? asset.getLocation().getName() : null)
                .assigneePublicId(asset.getAssignee() != null ? asset.getAssignee().getPublicId() : null)
//                .peripherals(asset.getPeripherals().stream().map(p -> p.getName()).collect(Collectors.toList()))
                .mediaUrls(asset.getMedia().stream().map(m -> m.getFileUrl()).collect(Collectors.toList()))
                .purchaseDate(asset.getPurchaseDate())
                .purchasedFrom(asset.getPurchasedFrom())
                .reservationStartDate(asset.getReservationStartDate())
                .reservationEndDate(asset.getReservationEndDate())
                .warrantyUntil(asset.getWarrantyUntil())
                .warrantyUntil(asset.getWarrantyUntil())
                .isAssignedToLocation(asset.getIsAssignedToLocation())
                .build();
    }

}

