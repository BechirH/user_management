package com.hsurvey.userservice.repositories;

import com.hsurvey.userservice.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByNameAndOrganizationId(String name, UUID organizationId);
    boolean existsByNameAndOrganizationId(String name, UUID organizationId);
    List<Role> findByOrganizationId(UUID organizationId);
    long countByOrganizationId(UUID organizationId);
    void deleteByOrganizationId(UUID organizationId);
}