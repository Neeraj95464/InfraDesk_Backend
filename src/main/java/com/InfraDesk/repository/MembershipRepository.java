package com.InfraDesk.repository;

import com.InfraDesk.entity.Company;
import com.InfraDesk.entity.Membership;
import com.InfraDesk.entity.User;
import com.InfraDesk.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership,Long> {
    boolean existsByUserAndCompany(User user, Company company);

    List<Membership> findByUserIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    Optional<Membership> findByUser_IdAndCompany_Id(Long userId, Long companyId);

    Page<Membership> findByUserIdAndIsActiveTrue(Long userId, Pageable pageable);

    // Finds active companies for a user with pagination via memberships relationship

    Page<Membership> findByUser_IdAndIsActiveTrue(Long userId, Pageable pageable);

    @Query("SELECT DISTINCT m.user FROM Membership m " +
            "WHERE m.company.id = :companyId AND m.isActive = true AND m.isDeleted = false")
    Page<User> findUsersByCompanyId(Long companyId, Pageable pageable);

    @Query("SELECT DISTINCT m.user FROM Membership m " +
            "WHERE m.company.publicId = :companyPublicId AND m.isActive = true AND m.isDeleted = false")
    Page<User> findUsersByCompanyPublicId(@Param("companyPublicId") String companyPublicId, Pageable pageable);

    @Query("""
  select distinct u from User u
  join u.memberships m
  where m.company.publicId = :companyId
    and u.role <> :excludedRole
""")
    Page<User> findUsersByCompanyPublicIdExcludingRole(@Param("companyId") String companyId, @Param("excludedRole") Role excludedRole, Pageable pageable);

    List<Membership> findByCompany_PublicIdAndRoleIsNotAndIsActiveTrueAndIsDeletedFalse(String companyPublicId, Role excludedRole);

    Page<Membership> findByCompany_PublicIdAndRoleIsNotAndIsActiveTrueAndIsDeletedFalse(
            String companyPublicId,
            Role excludedRole,
            org.springframework.data.domain.Pageable pageable
    );

    @Query(value = """
    select m from Membership m
    join fetch m.company c
    where m.user.id = :userId
      and m.isActive = true
""",
            countQuery = "select count(m) from Membership m where m.user.id = :userId and m.isActive = true"
    )
    Page<Membership> findActiveByUserIdWithCompany(@Param("userId") Long userId, Pageable pageable);


    Optional<Membership> findByUserAndCompany(User user, Company company);

    Optional<Membership> findByUser_IdAndCompany_PublicId(Long userPublicId, String companyPublicId);

    List<Membership> findByCompanyIdAndRoleAndIsActiveTrue(Long companyId, Role role);
}
