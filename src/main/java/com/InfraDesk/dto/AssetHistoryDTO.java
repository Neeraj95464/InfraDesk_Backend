package com.InfraDesk.dto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AssetHistoryDTO {
    private Long id;
    private String assetPublicId;  // Assuming Asset entity has this
    private String fieldName;
    private String oldValue;
    private String newValue;
    private String note;
    private String modifiedBy;
    private LocalDateTime modifiedAt;
}

