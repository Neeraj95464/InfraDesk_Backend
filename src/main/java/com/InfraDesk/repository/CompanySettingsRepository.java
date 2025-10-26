package com.InfraDesk.repository;

import com.InfraDesk.entity.CompanySettings;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CompanySettingsRepository extends JpaRepository<CompanySettings, Long> {
    Optional<CompanySettings> findByCompanyIdAndIsDeletedFalse(String companyId);
}

