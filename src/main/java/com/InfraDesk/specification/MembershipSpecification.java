//package com.InfraDesk.specification;
//
//import com.InfraDesk.dto.MembershipFilterRequest;
//import com.InfraDesk.entity.Membership;
//import com.InfraDesk.entity.Company;
//import com.InfraDesk.entity.User;
//import jakarta.persistence.criteria.JoinType;
//import org.springframework.data.jpa.domain.Specification;
//
//public class MembershipSpecification {
//
//    public static Specification<Membership> filter(MembershipFilterRequest req, String companyId) {
//        return (root, query, cb) -> {
//            var predicates = cb.conjunction();
//
//            predicates.getExpressions().add(cb.equal(root.get("company").get("publicId"), companyId));
//
//            if (req.getIsActive() != null)
//                predicates.getExpressions().add(cb.equal(root.get("isActive"), req.getIsActive()));
//
//            if (req.getIsDeleted() != null)
//                predicates.getExpressions().add(cb.equal(root.get("isDeleted"), req.getIsDeleted()));
//
//            if (req.getRoles() != null && !req.getRoles().isEmpty())
//                predicates.getExpressions().add(root.get("role").in(req.getRoles()));
//
//
//            if (req.getUserEmail() != null && !req.getUserEmail().trim().isEmpty()) {
//                var userJoin = root.join("user", JoinType.INNER);
//                predicates.getExpressions().add(cb.equal(userJoin.get("email"), req.getUserEmail().trim()));
//            }
//
//            if (req.getCompanyName() != null && !req.getCompanyName().trim().isEmpty()) {
//                var companyJoin = root.join("company", JoinType.INNER);
//                predicates.getExpressions().add(cb.like(cb.lower(companyJoin.get("name")), "%" + req.getCompanyName().toLowerCase().trim() + "%"));
//            }
//
//            if (req.getCreatedBy() != null && !req.getCreatedBy().trim().isEmpty()) {
//                predicates.getExpressions().add(cb.equal(root.get("createdBy"), req.getCreatedBy().trim()));
//            }
//
//            if (req.getCreatedAfter() != null)
//                predicates.getExpressions().add(cb.greaterThanOrEqualTo(root.get("createdAt"), req.getCreatedAfter()));
//
//            if (req.getCreatedBefore() != null)
//                predicates.getExpressions().add(cb.lessThanOrEqualTo(root.get("createdAt"), req.getCreatedBefore()));
//
//            return predicates;
//        };
//    }
//}


package com.InfraDesk.specification;

import com.InfraDesk.dto.MembershipFilterRequest;
import com.InfraDesk.entity.Membership;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class MembershipSpecification {

    public static Specification<Membership> filter(MembershipFilterRequest req, String companyId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Mandatory company filter
            predicates.add(cb.equal(root.get("company").get("publicId"), companyId));

            // isActive filter
            if (req.getIsActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), req.getIsActive()));
            }

            // isDeleted filter
            if (req.getIsDeleted() != null) {
                predicates.add(cb.equal(root.get("isDeleted"), req.getIsDeleted()));
            }

            // Role filter
            if (req.getRoles() != null && !req.getRoles().isEmpty()) {
                predicates.add(root.get("role").in(req.getRoles()));
            }

            // User email filter
            if (req.getUserEmail() != null && !req.getUserEmail().trim().isEmpty()) {
                var userJoin = root.join("user", JoinType.INNER);
                predicates.add(cb.like(cb.lower(userJoin.get("email")),
                        "%" + req.getUserEmail().toLowerCase().trim() + "%"));
            }

            // Company name filter
            if (req.getCompanyName() != null && !req.getCompanyName().trim().isEmpty()) {
                var companyJoin = root.join("company", JoinType.INNER);
                predicates.add(cb.like(cb.lower(companyJoin.get("name")),
                        "%" + req.getCompanyName().toLowerCase().trim() + "%"));
            }

            // CreatedBy filter
            if (req.getCreatedBy() != null && !req.getCreatedBy().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("createdBy")),
                        "%" + req.getCreatedBy().toLowerCase().trim() + "%"));
            }

            // Date range filters
            if (req.getCreatedAfter() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), req.getCreatedAfter()));
            }
            if (req.getCreatedBefore() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), req.getCreatedBefore()));
            }

            if (req.getSearch() != null && !req.getSearch().trim().isEmpty()) {
                var keyword = "%" + req.getSearch().toLowerCase().trim() + "%";
                var userJoin = root.join("user", JoinType.INNER);

                // Assuming name comes from user.employeeProfiles[0].name
                // If your User entity has a direct "name" field, use userJoin.get("name")
                var employeeProfilesJoin = userJoin.join("employeeProfiles", JoinType.LEFT);

                Predicate emailLike = cb.like(cb.lower(userJoin.get("email")), keyword);
                Predicate nameLike = cb.like(cb.lower(employeeProfilesJoin.get("name")), keyword);

                predicates.add(cb.or(emailLike, nameLike));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

