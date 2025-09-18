package com.InfraDesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentRequestDTO {

    @NotBlank(message = "Department name is required")
    @Size(max = 50, message = "Department name must not exceed 50 characters")
    private String name;

    private Boolean isActive = true; // Optional, default to active
}
