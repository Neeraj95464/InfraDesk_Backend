package com.InfraDesk.dto;

import com.InfraDesk.enums.AssetStatus;
import com.InfraDesk.enums.AssetType;
import lombok.Data;

@Data
public class AssetFilterRequest {
    private String name;
    private String assetTag;
    private AssetType assetType;
    private AssetStatus status;
    private String locationId;
    private String siteId;
    private Boolean isAssignedToLocation;
    private int page = 0;
    private int size = 20;
}

