package com.hsurvey.userservice.service.impl;

import com.hsurvey.userservice.dto.AuthRequest;
import com.hsurvey.userservice.dto.AuthResponse;
import com.hsurvey.userservice.dto.RegisterRequest;
import com.hsurvey.userservice.entities.Role;
import com.hsurvey.userservice.entities.User;
import com.hsurvey.userservice.repositories.UserRepository;
import com.hsurvey.userservice.service.AuthService;
import com.hsurvey.userservice.service.CustomUserDetailsService;
import com.hsurvey.userservice.service.OrganizationRoleService;
import com.hsurvey.userservice.service.clients.OrganizationClient;
import com.hsurvey.userservice.utils.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final OrganizationClient organizationClient;
    private final OrganizationRoleService organizationRoleService;

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
                throw new IllegalArgumentException("Organization not found");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify organization", e);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Create default roles and permissions for organization if they don't exist
        organizationRoleService.createDefaultRolesForOrganization(orgId);

        // Get default user role for the organization
        Role defaultUserRole = organizationRoleService.getDefaultUserRole(orgId);

        // Create new user with default role
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .organizationId(orgId)
                .roles(Set.of(defaultUserRole))
                .build();

        User savedUser = userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());

        // FIXED: Now passing organizationId to generateToken
        String jwtToken = jwtUtil.generateToken(userDetails, orgId);

        return AuthResponse.builder()
                .success(true)
                .token(jwtToken)
                .username(savedUser.getUsername())
                .organizationId(orgId)
                .message("User registered successfully")
                .build();
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

            // FIXED: Get the user's organization ID for token generation
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            UUID organizationId = user.getOrganizationId();

            // FIXED: Now passing organizationId to generateToken
            String jwtToken = jwtUtil.generateToken(userDetails, organizationId);

            return AuthResponse.builder()
                    .success(true)
                    .token(jwtToken)
                    .username(userDetails.getUsername())
                    .organizationId(organizationId) // ADDED: Include organization ID in response
                    .message("Login successful")
                    .build();
        } catch (AuthenticationException e) {
            throw new IllegalArgumentException("Authentication failed: Invalid email or password");
        }
    }
}