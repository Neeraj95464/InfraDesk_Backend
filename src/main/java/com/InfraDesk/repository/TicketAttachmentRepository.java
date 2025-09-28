package com.InfraDesk.repository;

import com.InfraDesk.entity.TicketAttachment;
import com.InfraDesk.entity.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TicketAttachmentRepository extends JpaRepository<TicketAttachment, Long> {


    Optional<TicketAttachment> findByPublicIdAndTicket_Company_PublicId(String publicId, String companyId);

}
