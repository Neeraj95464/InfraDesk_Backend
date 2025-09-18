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
public class LocationRequestDTO {

    @NotBlank(message = "Location name is required")
    @Size(max = 100, message = "Max 100 characters for location name")
    private String name;

    @Size(max = 255, message = "Max 255 characters for description")
    private String description;

    private Boolean isActive;
}

