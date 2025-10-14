package com.InfraDesk.repository;

import com.InfraDesk.entity.AssetPeripheral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetPeripheralRepository extends JpaRepository<AssetPeripheral,Long> {
}
