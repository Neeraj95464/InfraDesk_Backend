package com.InfraDesk.specification;


import com.InfraDesk.entity.Asset;
import com.InfraDesk.dto.AssetFilterRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class AssetSpecification {

    public static Specification<Asset> filterAssets(AssetFilterRequest req, String companyId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // company must match
            predicates.add(cb.equal(root.get("company").get("publicId"), companyId));

            if (req.getName() != null) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + req.getName().toLowerCase() + "%"));
            }

            if (req.getAssetTag() != null) {
                predicates.add(cb.equal(root.get("assetTag"), req.getAssetTag()));
            }

            if (req.getAssetType() != null) {
                predicates.add(cb.equal(root.get("assetType"), req.getAssetType()));
            }

            if (req.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), req.getStatus()));
            }

            if (req.getLocationId() != null) {
                predicates.add(cb.equal(root.get("location").get("publicId"), req.getLocationId()));
            }

            if (req.getSiteId() != null) {
                predicates.add(cb.equal(root.get("site").get("publicId"), req.getSiteId()));
            }

            if (req.getIsAssignedToLocation() != null) {
                predicates.add(cb.equal(root.get("isAssignedToLocation"), req.getIsAssignedToLocation()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

