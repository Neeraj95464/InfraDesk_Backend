package com.InfraDesk.repository;

import com.InfraDesk.entity.Company;
import com.InfraDesk.entity.Department;
import com.InfraDesk.entity.Site;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SiteRepository extends JpaRepository<Site, Long> {

    // For paginated fetch of sites (excluding soft deleted)
    Page<Site> findByCompanyAndIsDeletedFalse(Company company, Pageable pageable);

    // For uniqueness check (excluding soft deleted)
    boolean existsByNameAndCompanyAndIsDeletedFalse(String name, Company company);

    // For fetching by publicId, company, and not soft deleted
    Optional<Site> findByPublicIdAndCompanyAndIsDeletedFalse(String publicId, Company company);

    // Optionally, fetch even the soft deleted one (for restore logic if required)
    Optional<Site> findByNameAndCompanyAndIsDeletedTrue(String name, Company company);

    Optional<Site> findByPublicIdAndCompany_PublicId(String sitePublicId, String companyPublicId);

//    Optional<Site> findByPublicIdAndCompanyAndIsDeletedFalse(String publicId, Company company);

}
