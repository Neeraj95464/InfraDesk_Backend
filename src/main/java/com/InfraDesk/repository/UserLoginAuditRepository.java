package com.InfraDesk.repository;

import com.InfraDesk.entity.UserLoginAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface UserLoginAuditRepository extends JpaRepository<UserLoginAudit,Long> {
    @Query("""
  select max(a.loginTimestamp) from UserLoginAudit a where a.user.id = :userId and a.successful = true
""")
    LocalDateTime findLastLoginByUserId(@Param("userId") Long userId);

}
