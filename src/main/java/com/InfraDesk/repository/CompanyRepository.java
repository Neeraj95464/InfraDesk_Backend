package com.InfraDesk.repository;

import com.InfraDesk.entity.Company;
import com.InfraDesk.entity.CompanyDomain;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    boolean existsByDomain(String domain);
    boolean existsByContactEmail(String email);

    boolean existsByName(String companyName);

    // Find only non-deleted companies with pagination
    Page<Company> findByIsDeletedFalse(Pageable pageable);

        Optional<Company> findByIdAndIsDeletedFalse(Long id);

        Optional<Company> findByDomainAndIsDeletedFalse(String domain);

    List<Company> findByParentCompany_PublicIdAndIsActiveTrueAndIsDeletedFalse(String parentPublicId);

    Optional<Company> findByPublicId(String publicId);

    // in CompanyRepository.java
    List<Company> findByParentCompanyId(Long parentCompanyId);



}

