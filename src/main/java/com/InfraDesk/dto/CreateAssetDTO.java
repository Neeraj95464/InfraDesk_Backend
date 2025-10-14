package com.InfraDesk.dto;

import com.InfraDesk.enums.AssetStatus;
import com.InfraDesk.enums.AssetType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateAssetDTO {
    private String name;
    private String serialNumber;
    private String assetTag; // optional — if null system will generate
    private AssetStatus status;
    private String brand;
    private String model;
    private String note;
    private AssetType assetType;
    private BigDecimal cost;
    private String locationId;
    private String siteId;
    private LocalDate reservationStartDate;
    private LocalDate reservationEndDate;
    private Boolean isAssignedToLocation;
    private String description;
    private Long parentAssetId; // optional
    private Long assigneeEmployeeId;
    private LocalDate purchaseDate;
    private String purchasedFrom;
    private LocalDate warrantyUntil;
}

