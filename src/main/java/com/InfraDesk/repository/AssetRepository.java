package com.InfraDesk.repository;

import com.InfraDesk.entity.Asset;
import com.InfraDesk.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long>, JpaSpecificationExecutor<Asset> {
    Optional<Asset> findByPublicId(String publicId);
    boolean existsByAssetTagAndCompany_IdAndIsDeletedFalse(String assetTag, Long companyId);

    boolean existsByAssetTagAndCompany_PublicIdAndIsDeletedFalse(String tag, String companyId);
}

