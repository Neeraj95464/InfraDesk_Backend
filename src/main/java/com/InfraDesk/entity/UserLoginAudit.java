package com.InfraDesk.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_login_audit", indexes = {
        @Index(name = "idx_user_login_user", columnList = "user_id"),
        @Index(name = "idx_user_login_company", columnList = "company_id"),
        @Index(name = "idx_user_login_timestamp", columnList = "login_timestamp")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class UserLoginAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User who logged in
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    // Optional company context (if user logged in for a particular company/membership)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", updatable = false)
    private Company company;

    @CreatedDate
    @Column(name = "login_timestamp", nullable = false, updatable = false)
    private LocalDateTime loginTimestamp;

    @Column(name = "ip_address", length = 45) // Supports IPv6
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent; // Browser/Device info

    @Column(name = "login_location", length = 255)
    private String loginLocation; // Optional, e.g., city or geolocation info

    @Column(name = "successful", nullable = false)
    private Boolean successful; // True if login succeeded, false otherwise

    // Additional fields like failure reason, session id, etc. can be added
}
