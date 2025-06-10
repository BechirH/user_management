package com.hsurvey.userservice.repositories;

import com.hsurvey.userservice.entities.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    Optional<Permission> findByNameAndOrganizationId(String name, UUID organizationId);
    boolean existsByNameAndOrganizationId(String name, UUID organizationId);
    List<Permission> findByOrganizationId(UUID organizationId);
    long countByOrganizationId(UUID organizationId);
    void deleteByOrganizationId(UUID organizationId);
}