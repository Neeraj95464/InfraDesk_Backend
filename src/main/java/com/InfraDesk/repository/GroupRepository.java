package com.InfraDesk.repository;

import com.InfraDesk.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface GroupRepository extends JpaRepository<Group,Long> {
    Optional<Group> findByName(String name);

    List<Group> findByCompanyIdAndIsActiveTrue(Long companyId);

    Optional<Group> findByIdAndCompanyId(Long groupId, Long companyId);

    boolean existsByCompanyIdAndNameIgnoreCase(Long companyId, String name);

    List<Group> findByCompanyPublicIdAndIsActiveTrue(String companyId);

    Optional<Group> findByIdAndCompanyPublicId(Long groupId, String companyId);

    boolean existsByCompanyPublicIdAndNameIgnoreCase(String companyId, String name);

//    List<Group> findByPublicIdIn(Collection<String> publicIds);
    List<Group> findByPublicIdIn(Set<String> publicIds);
}
