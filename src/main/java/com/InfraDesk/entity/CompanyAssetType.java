package com.InfraDesk.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "company_asset_types",
        uniqueConstraints = @UniqueConstraint(columnNames = {"company_id", "type_name"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyAssetType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private String companyId;

    @Column(name = "type_name", nullable = false, length = 50)
    private String typeName;  // e.g. "Laptop", "Vehicle", "Furniture"

    @Column(name = "description", length = 250)
    private String description;
}
