package com.InfraDesk.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class LocationWithSitesDTO {
    private String publicId;
    private String name;
    private String description;
    private Boolean isActive;

    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;

    // Primary site info
    private String primarySitePublicId;
    private String primarySiteName;

    // All linked sites (publicId and name)
    private List<SiteInfo> linkedSites;

    @Data
    @Builder
    public static class SiteInfo {
        private String sitePublicId;
        private String siteName;
    }
}
