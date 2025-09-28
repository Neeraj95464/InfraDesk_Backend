package com.InfraDesk.repository;

import com.InfraDesk.entity.Company;
import com.InfraDesk.entity.Location;
import com.InfraDesk.entity.Site;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {

    // Pagination with company & site, exclude soft deleted
    Page<Location> findBySiteAndCompanyAndIsDeletedFalse(Site site, Company company, Pageable pageable);

    // Check if active location exists by name/site/company (for uniqueness)
    boolean existsByNameAndSiteAndCompanyAndIsDeletedFalse(String name, Site site, Company company);

    // Find a soft deleted location by name/site/company
    Optional<Location> findByNameAndSiteAndCompanyAndIsDeletedTrue(String name, Site site, Company company);

    // Find active location by public id, site and company
    Optional<Location> findByPublicIdAndSiteAndCompanyAndIsDeletedFalse(String publicId, Site site, Company company);


        Page<Location> findByCompanyAndIsDeletedFalse(Company company, Pageable pageable);

        Page<Location> findByCompanyAndIsDeletedFalseAndNameContainingIgnoreCase(Company company, String name, Pageable pageable);

    Optional<Location> findByPublicIdAndCompanyAndIsDeletedFalse(String locationPublicId, Company company);

    // Other existing methods...

    // In LocationRepository.java
    @Query("""
    select distinct l from Location l
    join l.assignedSites sla
    where l.company = :company
      and l.isDeleted = false
      and sla.isDeleted = false
""")
    Page<Location> findMultiSiteLinkedLocationsByCompany(@Param("company") Company company, Pageable pageable);

    @Query("""
    select distinct l from Location l
    join l.assignedSites sla
    where l.company = :company
      and l.isDeleted = false
      and sla.isDeleted = false
      and lower(l.name) like lower(concat('%',:name,'%'))
""")
    Page<Location> findMultiSiteLinkedLocationsByCompanyAndNameContainingIgnoreCase(@Param("company") Company company, @Param("name") String name, Pageable pageable);

    Optional<Location> findByPublicIdAndCompany_PublicId(String locationPublicId, String companyPublicId);

    List<Location> findByCompany_PublicId(String companyId);
}
