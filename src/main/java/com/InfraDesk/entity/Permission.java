


package com.InfraDesk.entity;

import com.InfraDesk.enums.PermissionCode;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {
    @Id
    @Enumerated(EnumType.STRING)
    @Column(length = 64)
    private PermissionCode code;

    @Column(length = 255, nullable = false)
    private String description;
}



