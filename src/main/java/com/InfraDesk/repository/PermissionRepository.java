//package com.InfraDesk.repository;
//
//import com.InfraDesk.entity.Permission;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//
//public interface PermissionRepository extends JpaRepository<Permission, String> {}


package com.InfraDesk.repository;

import com.InfraDesk.entity.Permission;
import com.InfraDesk.enums.PermissionCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, PermissionCode> {}

