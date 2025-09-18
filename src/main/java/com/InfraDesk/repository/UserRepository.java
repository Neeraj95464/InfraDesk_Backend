package com.InfraDesk.repository;

import com.InfraDesk.entity.User;
import com.InfraDesk.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);
//    List<User> findByRoleIsNotNullAndRoleNot(Role role);
Page<User> findByRoleIsNotNullAndRoleNot(Role role, Pageable pageable);

    boolean existsByEmail(String contactEmail);

    Page<User> findAll(Specification<User> spec, Pageable pageable);

    List<User> findByPublicIdIn(Set<String> publicIds);

    Optional<User> findByPublicId(String publicId);

}
