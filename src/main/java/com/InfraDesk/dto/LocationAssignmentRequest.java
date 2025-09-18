package com.InfraDesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationAssignmentRequest {

    @NotBlank
    private String sitePublicId;

    @NotNull
    private Long departmentId;

    @NotNull
    private Long executiveId;

    @NotNull
    private Long managerId;
}

