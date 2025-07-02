package com.hsurvey.userservice.repositories;

import com.hsurvey.userservice.entities.Role;
import com.hsurvey.userservice.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsernameAndOrganizationId(String username, UUID organizationId);
    boolean existsByEmailAndOrganizationId(String email, UUID organizationId);
    List<User> findByOrganizationId(UUID organizationId);
    Optional<User> findByIdAndOrganizationId(UUID id, UUID organizationId);
    boolean existsByOrganizationIdAndRolesContaining(UUID organizationId, Role role);
    @Modifying
    @Query(value = "INSERT INTO user_roles (user_id, role_id) VALUES (:userId, :roleId) ON CONFLICT DO NOTHING", nativeQuery = true)
    void addRoleToUser(@Param("userId") UUID userId, @Param("roleId") UUID roleId);

    @Modifying
    @Query(value = "DELETE FROM user_roles WHERE user_id = :userId AND role_id = :roleId", nativeQuery = true)
    void removeRoleFromUser(@Param("userId") UUID userId, @Param("roleId") UUID roleId);

    // Method to check if user-role relationship exists
    @Query(value = "SELECT COUNT(*) > 0 FROM user_roles WHERE user_id = :userId AND role_id = :roleId", nativeQuery = true)
    boolean hasRole(@Param("userId") UUID userId, @Param("roleId") UUID roleId);
}