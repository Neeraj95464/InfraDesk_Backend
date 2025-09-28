package com.InfraDesk.repository;

import com.InfraDesk.entity.MailIntegration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MailIntegrationRepository extends JpaRepository<MailIntegration, Long> {
    List<MailIntegration> findByEnabledTrue();
    Optional<MailIntegration> findByCompanyIdAndMailboxEmail(Long companyId, String mailboxEmail);
}

