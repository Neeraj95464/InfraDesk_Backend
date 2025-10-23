package com.InfraDesk.specification;

import com.InfraDesk.dto.EmployeeFilterRequest;
import com.InfraDesk.entity.Employee;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class EmployeeSpecification {

    public static Specification<Employee> filter(EmployeeFilterRequest req, String companyId) {
        return (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();

            // ✅ Tenant filtering
            predicates.add(cb.equal(root.get("company").get("publicId"), companyId));

            // ✅ isActive
            if (req.getIsActive() != null)
                predicates.add(cb.equal(root.get("isActive"), req.getIsActive()));

            // ✅ isDeleted
            if (req.getIsDeleted() != null)
                predicates.add(cb.equal(root.get("isDeleted"), req.getIsDeleted()));

            // ✅ Search by name or email
            if (req.getNameOrEmail() != null && !req.getNameOrEmail().trim().isEmpty()) {
                var userJoin = root.join("user", JoinType.LEFT);
                var nameMatch = cb.like(cb.lower(root.get("name")), "%" + req.getNameOrEmail().toLowerCase().trim() + "%");
                var emailMatch = cb.like(cb.lower(userJoin.get("email")), "%" + req.getNameOrEmail().toLowerCase().trim() + "%");
                predicates.add(cb.or(nameMatch, emailMatch));
            }

            // ✅ Department name
            if (req.getDepartmentName() != null && !req.getDepartmentName().trim().isEmpty()) {
                var deptJoin = root.join("department", JoinType.LEFT);
                predicates.add(cb.like(cb.lower(deptJoin.get("name")), "%" + req.getDepartmentName().toLowerCase().trim() + "%"));
            }

            // ✅ Designation
            if (req.getDesignation() != null && !req.getDesignation().trim().isEmpty())
                predicates.add(cb.like(cb.lower(root.get("designation")), "%" + req.getDesignation().toLowerCase().trim() + "%"));

            // ✅ Site name
            if (req.getSiteName() != null && !req.getSiteName().trim().isEmpty()) {
                var siteJoin = root.join("site", JoinType.LEFT);
                predicates.add(cb.like(cb.lower(siteJoin.get("name")), "%" + req.getSiteName().toLowerCase().trim() + "%"));
            }

            // ✅ Location name
            if (req.getLocationName() != null && !req.getLocationName().trim().isEmpty()) {
                var locJoin = root.join("location", JoinType.LEFT);
                predicates.add(cb.like(cb.lower(locJoin.get("name")), "%" + req.getLocationName().toLowerCase().trim() + "%"));
            }

            // ✅ Created by
            if (req.getCreatedBy() != null && !req.getCreatedBy().trim().isEmpty())
                predicates.add(cb.equal(root.get("createdBy"), req.getCreatedBy().trim()));

            // ✅ Date filters
            if (req.getCreatedAfter() != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), req.getCreatedAfter()));

            if (req.getCreatedBefore() != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), req.getCreatedBefore()));

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}
