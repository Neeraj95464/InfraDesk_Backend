package com.InfraDesk.specification;

import com.InfraDesk.dto.TicketFilterRequest;
import com.InfraDesk.entity.Ticket;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class TicketSpecification {
    public static Specification<Ticket> filterTickets(TicketFilterRequest req, String companyId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // company mandatory
            predicates.add(cb.equal(root.get("company").get("publicId"), companyId));

            if (req.getStatus() != null)
                predicates.add(cb.equal(root.get("status"), req.getStatus()));

            if (req.getPriority() != null)
                predicates.add(cb.equal(root.get("priority"), req.getPriority()));

            if (req.getDepartmentId() != null)
                predicates.add(cb.equal(root.get("department").get("publicId"), req.getDepartmentId()));

            if (req.getCreatedByUserId() != null)
                predicates.add(cb.equal(root.get("createdBy").get("publicId"), req.getCreatedByUserId()));

            if (req.getAssigneeUserId() != null)
                predicates.add(cb.equal(root.get("assignee").get("publicId"), req.getAssigneeUserId()));

            if (req.getLocationId() != null)
                predicates.add(cb.equal(root.get("location").get("publicId"), req.getLocationId()));

            if (req.getTicketTypeId() != null)
                predicates.add(cb.equal(root.get("ticketType").get("publicId"), req.getTicketTypeId()));

            if (req.getCreatedStart() != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), req.getCreatedStart()));

            if (req.getCreatedEnd() != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), req.getCreatedEnd()));

            if (req.getKeyword() != null && !req.getKeyword().isEmpty()) {
                String pattern = "%" + req.getKeyword().toLowerCase() + "%";
                predicates.add(
                        cb.or(
                                cb.like(cb.lower(root.get("subject")), pattern),
                                cb.like(cb.lower(root.get("description")), pattern)
                        )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
