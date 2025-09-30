package com.InfraDesk.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
//@Table(name = "mail_integrations",
//        indexes = {@Index(name = "idx_mail_company", columnList = "company_id")})
@Table(name = "mail_integrations",
        indexes = {@Index(name = "idx_mail_company", columnList = "company_id")},
        uniqueConstraints = {@UniqueConstraint(columnNames = {"company_id", "mailbox_email"})})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MailIntegration {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String companyId; // FK to your Company table (store id or publicId depending on design)

    @Column(nullable = false, length = 50)
    private String provider; // GMAIL | MICROSOFT | SMTP | IMAP

    @Column(nullable = false, length = 255)
    private String mailboxEmail; // e.g. support@acme.com

    // OAuth fields (encrypted)
    @Column(length = 4000)
    private String encryptedAccessToken;

    @Column(length = 4000)
    private String encryptedRefreshToken;

    private Instant tokenExpiresAt;

    // For SMTP / IMAP fallback
    @Column(length = 255)
    private String encryptedSmtpPassword;

    @Column(length = 255)
    private String smtpHost;
    private Integer smtpPort;
    private Boolean smtpTls;

    @Column(length = 4000)
    private String scopes; // e.g. "gmail.readonly,gmail.send"

    private Boolean enabled = true;

    private Instant lastSyncAt;

    private String providerExtra; // JSON for provider-specific extra fields

    private Instant createdAt;
    private Instant updatedAt;
}

