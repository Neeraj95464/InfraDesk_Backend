package com.InfraDesk.repository;

import com.InfraDesk.entity.Ticket;
import com.InfraDesk.entity.TicketType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {
    List<TicketType> findByCompanyId(Long companyId);
    List<TicketType> findByCompanyPublicIdAndActiveTrueAndCompanyIsActiveTrue(String companyPublicId);


//        List<TicketType> findByCompanyIdAndActiveTrue(Long companyId);
        Optional<TicketType> findByIdAndCompanyId(Long id, Long companyId);
        boolean existsByCompanyIdAndNameIgnoreCase(Long companyId, String name);

    // 1. Search with filter (company publicId + name search + active = true)
    Page<TicketType> findByCompanyPublicIdAndNameContainingIgnoreCaseAndActiveTrue(
            String companyPublicId,
            String name,
            Pageable pageable
    );
    Optional<TicketType> findByCompanyPublicIdAndNameContainingIgnoreCaseAndActiveTrue(
            String companyPublicId,
            String name
    );

    // 2. All active by company publicId (no search filter)
    Page<TicketType> findByCompanyPublicIdAndActiveTrue(
            String companyPublicId,
            Pageable pageable
    );

    Optional<TicketType>findByPublicIdAndCompanyPublicId(String id, String companyId);

    boolean existsByCompanyPublicIdAndNameIgnoreCase(String companyId, String name);

    Optional<TicketType> findByCompanyPublicId(String companyId);

    Optional<TicketType> findByPublicIdAndCompany_PublicId(String publicId, String companyPublicId);

    List<TicketType> findByCompany_PublicId(String companyId);
}
