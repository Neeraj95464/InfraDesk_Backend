


package com.InfraDesk.specification;

import com.InfraDesk.entity.User;
import com.InfraDesk.entity.Employee;
import com.InfraDesk.entity.Membership;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.Set;

public class UserSpecification {

    public static Specification<User> userHasKeywordAndAnyCompany(Set<String> companyPublicIds, String keyword) {
        return (root, query, cb) -> {
            if (companyPublicIds == null || companyPublicIds.isEmpty()) {
                return cb.disjunction();
            }

            // Join memberships to filter by companies
            Join<User, Membership> membershipJoin = root.join("memberships", JoinType.INNER);

            Predicate companyMatch = membershipJoin.get("company").get("publicId").in(companyPublicIds);

            if (keyword == null || keyword.isBlank()) {
                return companyMatch;
            }

            String likePattern = "%" + keyword.toLowerCase() + "%";
            Join<User, Employee> empJoin = root.join("employeeProfiles", JoinType.LEFT);

            Predicate searchPredicate = cb.or(
                    cb.like(cb.lower(root.get("email")), likePattern),
                    cb.like(cb.lower(root.get("publicId")), likePattern),
                    cb.like(cb.lower(empJoin.get("employeeId")), likePattern),
                    cb.like(cb.lower(empJoin.get("name")), likePattern),
                    cb.like(cb.lower(empJoin.get("phone")), likePattern)
            );

            return cb.and(companyMatch, searchPredicate);
        };
    }



}
