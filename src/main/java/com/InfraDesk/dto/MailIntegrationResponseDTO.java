package com.InfraDesk.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

// DTO for response
@Getter
@Setter
@Builder
public class MailIntegrationResponseDTO {
    private String provider;
    private String mailboxEmail;
    private Boolean enabled;
    private Instant createdAt;
}

