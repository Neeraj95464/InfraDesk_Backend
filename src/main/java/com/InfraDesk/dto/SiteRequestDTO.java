package com.InfraDesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteRequestDTO {

    @NotBlank(message = "Site name is required")
    @Size(max = 100, message = "Max 100 chars for site name")
    private String name;

    @Size(max = 255, message = "Max 255 chars for address")
    private String address;

    private Boolean isActive;
}

