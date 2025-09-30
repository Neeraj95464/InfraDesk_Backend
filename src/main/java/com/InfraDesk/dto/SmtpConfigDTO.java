package com.InfraDesk.dto;

import lombok.Data;

@Data
public class SmtpConfigDTO {
    private String companyId;
    private String mailboxEmail;
    private String smtpHost;
    private Integer smtpPort;
    private Boolean smtpTls; // true if TLS enabled
    private String smtpPassword;
}

