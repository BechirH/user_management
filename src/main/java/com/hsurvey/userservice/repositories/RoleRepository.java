package com.hsurvey.userservice.repositories;

import com.hsurvey.userservice.entities.Permission;
import com.hsurvey.userservice.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByNameAndOrganizationId(String name, UUID organizationId);
    boolean existsByNameAndOrganizationId(String name, UUID organizationId);
    List<Role> findByOrganizationId(UUID organizationId);

    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p = :permission")
    List<Role> findByPermissionsContaining(@Param("permission") Permission permission);
}