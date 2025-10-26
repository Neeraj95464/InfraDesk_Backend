package com.InfraDesk.repository;

import com.InfraDesk.entity.CompanyAssetType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CompanyAssetTypeRepository extends JpaRepository<CompanyAssetType, Long> {
    List<CompanyAssetType> findByCompanyId(String companyId);
    boolean existsByCompanyIdAndTypeName(String companyId, String typeName);
    Optional<CompanyAssetType> findByCompanyIdAndTypeName(String companyId, String typeName);

}

