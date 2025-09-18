//package com.InfraDesk.repository;
//
//import com.InfraDesk.entity.CompanyDomain;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.Optional;
//import java.util.Set;
//
//public interface CompanyDomainRepository extends JpaRepository<CompanyDomain, Long> {
//    boolean existsByDomain(String domain);
//
//    List<CompanyDomain> findAllByDomainIn(Set<String> domains);
//
//    List<CompanyDomain> findByCompanyIdAndIsDeletedFalse(Long currentCompanyId);
//
//    boolean existsByDomainAndIsActiveTrueAndIsDeletedFalse(String normalized);
//
//    Page<CompanyDomain> findByCompany_IdAndIsDeletedFalse(Long companyId, Pageable pageable);
//
//    Optional<CompanyDomain> findByIdAndCompany_IdAndIsDeletedFalse(Long domainId, Long companyId);
//
//    Page<CompanyDomain> findByCompany_PublicIdAndIsDeletedFalse(String companyId, Pageable pageable);
//
//    Optional<CompanyDomain> findByPublicIdAndCompany_PublicIdAndIsDeletedFalse(String domainId, String companyId);
//}
//
//


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
import java.util.Set;

public interface CompanyDomainRepository extends JpaRepository<CompanyDomain, Long> {

    // Pagination by Company and isDeleted = false
    Page<CompanyDomain> findByCompanyAndIsDeletedFalse(Company company, Pageable pageable);

    List<CompanyDomain> findAllByDomainIn(Set<String> domains);

    Optional<CompanyDomain> findByDomainIgnoreCase(String domain);

    // Find domain by publicId and company with not deleted
    Optional<CompanyDomain> findByPublicIdAndCompany_PublicIdAndIsDeletedFalse(String publicId, String companyPublicId);

    List<CompanyDomain> findByCompany_PublicIdAndIsActiveTrueAndIsDeletedFalse(String publicId);

    // Optional helper for checking domain existence excluding a particular publicId for update scenarios
    boolean existsByDomainAndIsActiveTrueAndIsDeletedFalseAndPublicIdNot(String domain, String publicId);

    Optional<CompanyDomain> findByDomain(String normalized);


}
