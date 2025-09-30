package com.InfraDesk.repository;

import com.InfraDesk.entity.TicketMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketMessageRepository extends JpaRepository<TicketMessage, Long> {
    List<TicketMessage> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
    List<TicketMessage> findByTicket_PublicIdOrderByCreatedAtAsc(String ticketPublicId);

    Page<TicketMessage> findByTicket_PublicIdAndTicket_Company_PublicIdOrderByCreatedAtAsc(String ticketId, String companyId, Pageable pageable);

    Optional<TicketMessage> findByEmailMessageId(String messageId);
}