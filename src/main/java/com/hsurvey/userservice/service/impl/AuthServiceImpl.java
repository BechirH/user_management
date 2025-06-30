package com.hsurvey.userservice.service.impl;

import com.hsurvey.userservice.dto.AuthRequest;
import com.hsurvey.userservice.dto.AuthResponse;
import com.hsurvey.userservice.dto.RegisterRequest;
import com.hsurvey.userservice.dto.AdminRegisterRequest;
import com.hsurvey.userservice.entities.Role;
import com.hsurvey.userservice.entities.User;
import com.hsurvey.userservice.exception.AdminAlreadyExistsException;
import com.hsurvey.userservice.repositories.UserRepository;
import com.hsurvey.userservice.service.AuthService;
import com.hsurvey.userservice.service.CustomUserDetailsService;
import com.hsurvey.userservice.service.OrganizationRoleService;
import com.hsurvey.userservice.service.clients.OrganizationClient;
import com.hsurvey.userservice.service.clients.DepartmentClient;
import com.hsurvey.userservice.service.clients.TeamClient;
import com.hsurvey.userservice.utils.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final OrganizationClient organizationClient;
    private final OrganizationRoleService organizationRoleService;
    private final DepartmentClient departmentClient;
    private final TeamClient teamClient;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        UUID orgId;
        try {
            orgId = UUID.fromString(request.getInviteCode());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid invite code format");
        }

        try {
            ResponseEntity<Boolean> response = organizationClient.organizationExists(orgId);
            if (response == null || !response.getStatusCode().is2xxSuccessful() ||
                    !Boolean.TRUE.equals(response.getBody())) {
                throw new EntityNotFoundException("Organization not found");
            }
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify organization", e);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        Role defaultUserRole = organizationRoleService.getDefaultUserRole(orgId);

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .organizationId(orgId)
                .roles(Set.of(defaultUserRole))
                .build();

        User savedUser = userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());

        // Fetch department and team IDs for the user
        UUID departmentId = getDepartmentIdForUser(savedUser.getId());
        UUID teamId = getTeamIdForUser(savedUser.getId());

        String jwtToken = jwtUtil.generateToken(userDetails, savedUser.getId(), orgId, departmentId, teamId);

        return AuthResponse.builder()
                .success(true)
                .token(jwtToken)
                .username(savedUser.getUsername())
                .organizationId(orgId)
                .roles(savedUser.getRoles().stream().map(Role::getName).toList())
                .message("User registered successfully")
                .build();
    }

    @Override
    @Transactional
    public AuthResponse registerAdmin(AdminRegisterRequest request, UUID organizationId) {

        try {
            ResponseEntity<Boolean> response = organizationClient.organizationExists(organizationId);
            if (response == null || !response.getStatusCode().is2xxSuccessful() ||
                    !Boolean.TRUE.equals(response.getBody())) {
                throw new EntityNotFoundException("Organization not found");
            }
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify organization", e);
        }


        if (adminAlreadyExistsForOrganization(organizationId)) {
            throw new AdminAlreadyExistsException("Admin user already exists for this organization");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        organizationRoleService.createDefaultRolesForOrganization(organizationId);

        Role adminRole = organizationRoleService.getDefaultAdminRole(organizationId);

        User adminUser = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .organizationId(organizationId)
                .roles(Set.of(adminRole))
                .build();

        User savedUser = userRepository.save(adminUser);

        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());

        // Fetch department and team IDs for the user
        UUID departmentId = getDepartmentIdForUser(savedUser.getId());
        UUID teamId = getTeamIdForUser(savedUser.getId());

        String jwtToken = jwtUtil.generateToken(userDetails, savedUser.getId(), organizationId, departmentId, teamId);

        return AuthResponse.builder()
                .success(true)
                .token(jwtToken)
                .username(savedUser.getUsername())
                .organizationId(organizationId)
                .roles(savedUser.getRoles().stream().map(Role::getName).toList())
                .message("Admin registered successfully")
                .build();
    }


    private boolean adminAlreadyExistsForOrganization(UUID organizationId) {
        Role adminRole;
        try {
            adminRole = organizationRoleService.getDefaultAdminRole(organizationId);
        } catch (RuntimeException e) {

            return false;
        }

        return userRepository.existsByOrganizationIdAndRolesContaining(organizationId, adminRole);
    }

    private UUID getDepartmentIdForUser(UUID userId) {
        try {
            ResponseEntity<UUID> response = departmentClient.getDepartmentIdByUserId(userId);
            if (response != null && response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.debug("Found department ID {} for user {}", response.getBody(), userId);
                return response.getBody();
            }
        } catch (Exception e) {

            log.debug("Could not fetch department ID for user {}: {}", userId, e.getMessage());
        }
        log.debug("No department ID found for user {}", userId);
        return null;
    }

    private UUID getTeamIdForUser(UUID userId) {
        try {
            ResponseEntity<UUID> response = teamClient.getTeamIdByUserId(userId);
            if (response != null && response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.debug("Found team ID {} for user {}", response.getBody(), userId);
                return response.getBody();
            }
        } catch (Exception e) {
            // Log the exception but don't fail the authentication
            // User might not be assigned to a team yet
            log.debug("Could not fetch team ID for user {}: {}", userId, e.getMessage());
        }
        log.debug("No team ID found for user {}", userId);
        return null;
    }

    @Override
    public AuthResponse authenticate(AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            UUID organizationId = user.getOrganizationId();

            // Fetch department and team IDs for the user
            UUID departmentId = getDepartmentIdForUser(user.getId());
            UUID teamId = getTeamIdForUser(user.getId());

            String jwtToken = jwtUtil.generateToken(userDetails, user.getId(), organizationId, departmentId, teamId);

            return AuthResponse.builder()
                    .success(true)
                    .token(jwtToken)
                    .username(userDetails.getUsername())
                    .organizationId(organizationId)
                    .roles(user.getRoles().stream().map(Role::getName).toList())
                    .message("Login successful")
                    .build();
        } catch (AuthenticationException e) {
            throw new IllegalArgumentException("Authentication failed: Invalid email or password");
        }
    }
}