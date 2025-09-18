package com.InfraDesk.repository;

import com.InfraDesk.entity.Company;
import com.InfraDesk.entity.Location;
import com.InfraDesk.entity.Site;
import com.InfraDesk.entity.SiteLocationAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SiteLocationAssignmentRepository extends JpaRepository<SiteLocationAssignment, Long> {
    boolean existsByCompanyAndLocationAndSiteAndIsDeletedFalse(
            Company company,
            Location location,
            Site site
    );

    List<SiteLocationAssignment> findAllBySiteAndCompanyAndIsDeletedFalse(Site site, Company company);


    Optional<SiteLocationAssignment> findByCompanyAndLocationAndSiteAndIsDeletedTrue(
            Company company, Location location, Site site);


    List<SiteLocationAssignment> findByLocationAndCompanyAndIsDeletedFalse(Location location, Company company);

}

