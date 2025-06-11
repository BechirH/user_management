package com.hsurvey.userservice.repositories;

import com.hsurvey.userservice.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
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
}