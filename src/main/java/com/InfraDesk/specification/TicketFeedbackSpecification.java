

package com.InfraDesk.specification;

import com.InfraDesk.dto.FeedbackFilterRequest;
import com.InfraDesk.entity.TicketFeedback;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class TicketFeedbackSpecification {
    public static Specification<TicketFeedback> filterFeedbacks(String companyId, FeedbackFilterRequest req) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by company via ticket's company.publicId
            if (companyId != null && !companyId.isEmpty()) {
                predicates.add(cb.equal(root.get("ticket").get("company").get("publicId"), companyId));
            }

//             Filter by assignee user ID on the related ticket's assignee publicId
            if (req.getAssigneeUserId() != null) {
                predicates.add(cb.equal(root.get("ticket").get("assignee").get("publicId"), req.getAssigneeUserId()));
            }

//            if (req.getAssigneeUserId() != null) {
////                Predicate dynamicAssigneePredicate = cb.equal(root.get("ticket").get("assignee").get("publicId"), req.getAssigneeUserId());
//                Predicate dynamicAssigneePredicate = null;
//                // Static predicate for demo user publicId "demo_user_id"
//                Predicate staticAssigneePredicate = cb.equal(root.get("ticket").get("assignee").get("publicId"), "demo_user_id");
//
//                predicates.add(cb.or(null, staticAssigneePredicate));
//            }
            // Filter by stars rating
            if (req.getStars() != null) {
                predicates.add(cb.equal(root.get("stars"), req.getStars()));
            }

            // Filter by feedback submitted date range
            if (req.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("submittedAt"), req.getFromDate()));
            }
            if (req.getToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("submittedAt"), req.getToDate()));
            }

            // Keyword search on feedbackText (case-insensitive)
            if (req.getKeyword() != null && !req.getKeyword().isEmpty()) {
                String pattern = "%" + req.getKeyword().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("feedbackText")), pattern));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
