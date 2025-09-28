//package com.InfraDesk.repository;
//
//
//import com.InfraDesk.entity.Company;
//import com.InfraDesk.entity.Permission;
//import com.InfraDesk.entity.RolePermission;
//import com.InfraDesk.enums.PermissionCode;
//import com.InfraDesk.enums.Role;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//import java.util.List;
//
//public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
////    List<RolePermission> findByCompanyIdAndRoleAndAllowedTrue(Long companyId, Role role);
//
////    List<RolePermission> findByCompanyIdAndRoleAndAllowedTrue(Long companyId, Role role);
//
//    @Query("SELECT rp.permission FROM RolePermission rp WHERE rp.company.id = :companyId AND rp.role = :role AND rp.allowed = true")
//    List<PermissionCode> findPermissionCodesByCompanyIdAndRole(@Param("companyId") Long companyId, @Param("role") Role role);
//
//
////    boolean existsByCompanyAndRoleAndPermission(Company company, Role role, Permission permission);
//
//    boolean existsByCompanyAndRoleAndPermission(Company company, Role role, Permission permission);
//
//    List<RolePermission> findByCompanyId(Long companyId); // changed from String to Long
//
////    @Query("SELECT rp FROM RolePermission rp WHERE rp.company.publicId = :publicId")
////    List<RolePermission> findByCompanyPublicId(@Param("publicId") String publicId);
//
//    @Query("SELECT rp FROM RolePermission rp WHERE rp.company.publicId = :publicId")
//    List<RolePermission> findByCompanyPublicId(@Param("publicId") String publicId);
//
//
////    @Query("SELECT p.code FROM RolePermission rp JOIN rp.permission p " +
////            "WHERE rp.company.id = :companyId AND rp.role = :role AND rp.allowed = true")
////    List<String> findPermissionCodesByCompanyIdAndRole(@Param("companyId") Long companyId,
////                                                       @Param("role") Role role);
//
//}
//


package com.InfraDesk.repository;

import com.InfraDesk.entity.Company;
import com.InfraDesk.entity.RolePermission;
import com.InfraDesk.enums.Role;
import com.InfraDesk.enums.PermissionCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    @Query("SELECT rp.permission FROM RolePermission rp WHERE rp.company.id = :companyId AND rp.role = :role AND rp.allowed = true")
    List<PermissionCode> findPermissionCodesByCompanyIdAndRole(@Param("companyId") Long companyId, @Param("role") Role role);

    boolean existsByCompanyAndRoleAndPermission(Company company, Role role, PermissionCode permission);

    List<RolePermission> findByCompanyId(Long companyId);

    @Query("SELECT rp FROM RolePermission rp WHERE rp.company.publicId = :publicId")
    List<RolePermission> findByCompanyPublicId(@Param("publicId") String publicId);

    void deleteAllByCompany_Id(Long companyId);  // convenient bulk deletion

    void deleteAllByCompany(Company company);
}
