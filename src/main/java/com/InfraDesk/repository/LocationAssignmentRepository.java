package com.InfraDesk.repository;

import com.InfraDesk.entity.Department;
import com.InfraDesk.entity.Location;
import com.InfraDesk.entity.LocationAssignment;
import com.InfraDesk.entity.Site;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationAssignmentRepository extends JpaRepository<LocationAssignment,Long> {
    boolean existsByLocationAndSiteAndDepartmentAndIsDeletedFalse(Location location, Site targetSite, Department department);
}
