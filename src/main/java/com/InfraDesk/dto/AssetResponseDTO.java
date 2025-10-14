package com.InfraDesk.dto;

import com.InfraDesk.enums.AssetStatus;
import com.InfraDesk.enums.AssetType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AssetResponseDTO {
    private String publicId;
    private String name;
    private String serialNumber;
    private String assetTag;
    private String description;
    private AssetType assetType;
    private String locationId;
    private String locationName;
    private String siteId;
    private String siteName;
    private AssetStatus status;
    private String companyPublicId;
    private String assigneePublicId;
    private LocalDate purchaseDate;
    private LocalDate warrantyUntil;
    private Boolean isAssignedToLocation;
    private String note;
    private String brand;
    private String model;
    private BigDecimal cost;
    private LocalDate reservationStartDate;
    private LocalDate reservationEndDate;
    private String parentAssetId;
    private String assigneeEmployeeId;
    private String assigneeEmployeeName;
    private String purchasedFrom;
    private List<String> mediaUrls;
}

