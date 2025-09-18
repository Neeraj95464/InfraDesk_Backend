package com.InfraDesk.dto;

import lombok.Builder;

@Builder
public record PreferencesDto(
        String defaultCompanyId,
        String language,
        String timezone
) {}

