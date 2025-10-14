package com.InfraDesk.repository;

import com.InfraDesk.entity.MailIntegration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MailIntegrationRepository extends JpaRepository<MailIntegration, Long> {
    List<MailIntegration> findByEnabledTrue();
    Optional<MailIntegration> findByCompanyIdAndMailboxEmail(String companyId, String mailboxEmail);

    Page<MailIntegration> findByCompanyId(String companyId, Pageable pageable);

    Optional<MailIntegration> findByPublicId(String publicId);
    Page<MailIntegration> findByCompanyIdAndIsDeletedFalse(String companyId,Pageable pageable);
}

