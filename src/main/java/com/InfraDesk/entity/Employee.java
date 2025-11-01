package com.InfraDesk.entity;

import com.InfraDesk.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @Column(nullable = false, unique = true, updatable = false, length = 50)
    private String publicId;

    @NotBlank(message = "Employee ID cannot be blank")
    @Size(max = 50, message = "Employee ID must be at most 50 characters")
    @Column(nullable = false, length = 50)
    private String employeeId;

    @NotBlank(message = "Name cannot be blank")
    @Size(max = 100, message = "Name must be at most 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Phone number is required")
    @Size(max = 15, message = "Phone number must be at most 15 digits")
    private String phone;

    @NotNull(message = "Department is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Size(max = 100, message = "Designation must be at most 100 characters")
    private String designation;

    @NotNull(message = "Company is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @NotNull(message = "User reference is required")
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

    @Size(max = 100, message = "CreatedBy must be at most 100 characters")
    private String createdBy;

    @NotNull
    private Boolean isActive = true;

    @NotNull
    private Boolean isDeleted = false;

    @LastModifiedDate
    private LocalDateTime updatedAt;

//    private static String generatePublicId() {
//        return "EMP-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
//    }

    private static final String PUBLIC_ID_PREFIX = "EMP-";
    private static final int PUBLIC_ID_LENGTH = 12;

    @PrePersist
    protected void onCreate() {
        if (publicId == null || publicId.isEmpty()) {
            publicId = PUBLIC_ID_PREFIX + UUID.randomUUID().toString().replace("-", "").substring(0, PUBLIC_ID_LENGTH).toUpperCase();
        }
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (isDeleted == null) {
            isDeleted = false;
        }
    }

}
