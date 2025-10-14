package com.InfraDesk.repository;

import com.InfraDesk.entity.AssetMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetMediaRepository extends JpaRepository<AssetMedia,Long> {
}
