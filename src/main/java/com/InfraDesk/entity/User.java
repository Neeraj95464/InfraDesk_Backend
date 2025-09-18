package com.InfraDesk.entity;

import com.InfraDesk.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                // Email must be globally unique
                @UniqueConstraint(name = "uc_user_email", columnNames = {"email"})
        },
        indexes = {
                // Optimizes login queries by email
                @Index(name = "idx_user_email", columnList = "email")
        }
)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"employeeProfiles"})
@EqualsAndHashCode(of = "id")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(nullable = false, unique = true, updatable = false, length = 50)
    private String publicId = generatePublicId();

    /** Globally unique email for login */
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    /** Global role like SUPER_ADMIN, COMPANY_CONFIGURE */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role = Role.USER;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Employee> employeeProfiles;

    // Add this for memberships
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Membership> memberships;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = true, length = 100)
    private String createdBy;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Boolean isDeleted = false;


    private static String generatePublicId() {
        return "USR-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }

}
