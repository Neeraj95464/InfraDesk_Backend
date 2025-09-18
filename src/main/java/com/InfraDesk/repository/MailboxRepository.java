package com.InfraDesk.repository;


import com.InfraDesk.entity.Mailbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MailboxRepository extends JpaRepository<Mailbox, Long> {
    Optional<Mailbox> findByEmailAddressIgnoreCase(String email);
}
