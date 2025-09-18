package com.InfraDesk.entity;

import com.InfraDesk.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "employees",
        uniqueConstraints = {
                // Ensures employeeId is unique within the company
                @UniqueConstraint(name = "uc_employee_company", columnNames = {"employeeId", "company_id"})
        },
        indexes = {
                // Fast lookup for employees by company
                @Index(name = "idx_employee_company", columnList = "company_id"),
                // Fast lookup by user (to fetch profiles)
                @Index(name = "idx_employee_user", columnList = "user_id"),
                // Fast lookup by department within company
                @Index(name = "idx_employee_department", columnList = "department_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"company", "department", "user", "site", "location"})
@EqualsAndHashCode(of = "id")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(nullable = false, unique = true, updatable = false, length = 50)
    private String publicId = generatePublicId();

    /** Unique employeeId per company (e.g., EMP001) */
    @Column(nullable = false, length = 50)
    private String employeeId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 15)
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(nullable = true, length = 100)
    private String designation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id")
    private Site site;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = true, length = 100)
    private String createdBy;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Boolean isDeleted = false;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private static String generatePublicId() {
        return "EMP-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }

}
