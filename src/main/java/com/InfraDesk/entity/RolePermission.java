package com.InfraDesk.entity;
import com.InfraDesk.enums.PermissionCode;
import com.InfraDesk.enums.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "role_permissions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"company_id","role","permission_code"})
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RolePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id")
    private Company company;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission_code", nullable = false)
    private PermissionCode permission;

    @Column(nullable = false)
    private boolean allowed = true;
}


