package com.hsurvey.userservice.service.impl;

import com.hsurvey.userservice.dto.AuthRequest;
import com.hsurvey.userservice.dto.AuthResponse;
import com.hsurvey.userservice.dto.RegisterRequest;
import com.hsurvey.userservice.dto.AdminRegisterRequest;
import com.hsurvey.userservice.entities.Role;
import com.hsurvey.userservice.entities.User;
import com.hsurvey.userservice.entities.RefreshToken;
import com.hsurvey.userservice.exception.AdminAlreadyExistsException;
import com.hsurvey.userservice.repositories.UserRepository;
import com.hsurvey.userservice.repositories.RefreshTokenRepository;
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

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import jakarta.servlet.http.HttpServletResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final OrganizationClient organizationClient;
    private final OrganizationRoleService organizationRoleService;
    private final DepartmentClient departmentClient;
    private final TeamClient teamClient;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    @Value("${jwt.refreshExpiration:604800000}") // 7 days default
    private long refreshExpiration;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request, HttpServletResponse response) {
        UUID orgId;
        try {
            orgId = UUID.fromString(request.getInviteCode());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid invite code format");
        }

        try {
            System.out.println("User Service - Verifying organization: " + orgId);
            ResponseEntity<Boolean> orgResponse = organizationClient.organizationExists(orgId);
            System.out.println("User Service - Organization response: " + orgResponse);
            if (orgResponse == null || !orgResponse.getStatusCode().is2xxSuccessful() ||
                    !Boolean.TRUE.equals(orgResponse.getBody())) {
                System.out.println("User Service - Organization verification failed");
                throw new EntityNotFoundException("Organization not found");
            }
            System.out.println("User Service - Organization verified successfully");
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            System.out.println("User Service - Organization verification error: " + e.getMessage());
            System.out.println("User Service - Exception type: " + e.getClass().getSimpleName());
            e.printStackTrace();
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

        // Generate JWT token with complete user context
        String jwtToken = generateJwtToken(savedUser, departmentId, teamId);
        RefreshToken refreshToken = createRefreshToken(savedUser);
        setAuthCookies(response, jwtToken, refreshToken.getToken());

        return AuthResponse.builder()
                .success(true)
                .username(savedUser.getUsername())
                .organizationId(orgId)
                .roles(savedUser.getRoles().stream().map(Role::getName).toList())
                .message("User registered successfully")
                .build();
    }

    @Override
    @Transactional
    public AuthResponse registerAdmin(AdminRegisterRequest request, UUID organizationId, HttpServletResponse response) {

        try {
            ResponseEntity<Boolean> orgResponse = organizationClient.organizationExists(organizationId);
            if (orgResponse == null || !orgResponse.getStatusCode().is2xxSuccessful() ||
                    !Boolean.TRUE.equals(orgResponse.getBody())) {
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

        // Generate JWT token with complete user context
        String jwtToken = generateJwtToken(savedUser, departmentId, teamId);
        RefreshToken refreshToken = createRefreshToken(savedUser);
        setAuthCookies(response, jwtToken, refreshToken.getToken());

        return AuthResponse.builder()
                .success(true)
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
            if (response != null && response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.warn("Failed to get department ID for user: {}", userId, e);
        }
        return null;
    }

    private UUID getTeamIdForUser(UUID userId) {
        try {
            ResponseEntity<UUID> response = teamClient.getTeamIdByUserId(userId);
            if (response != null && response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.warn("Failed to get team ID for user: {}", userId, e);
        }
        return null;
    }

    @Override
    public AuthResponse authenticate(AuthRequest request, HttpServletResponse response) {
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
            UUID departmentId = getDepartmentIdForUser(user.getId());
            UUID teamId = getTeamIdForUser(user.getId());
            
            // Generate JWT token with complete user context
            String jwtToken = generateJwtToken(user, departmentId, teamId);
            RefreshToken refreshToken = createRefreshToken(user);
            setAuthCookies(response, jwtToken, refreshToken.getToken());
            
            return AuthResponse.builder()
                    .success(true)
                    .username(userDetails.getUsername())
                    .organizationId(organizationId)
                    .roles(user.getRoles().stream().map(Role::getName).toList())
                    .message("Login successful")
                    .build();
        } catch (AuthenticationException e) {
            throw new IllegalArgumentException("Authentication failed: Invalid email or password");
        }
    }

    private String generateJwtToken(User user, UUID departmentId, UUID teamId) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        
        return jwtUtil.generateToken(
            userDetails,
            user.getId(),
            user.getOrganizationId(),
            departmentId,
            teamId
        );
    }

    public AuthResponse refreshAccessToken(String refreshToken, HttpServletResponse response) {
        RefreshToken tokenEntity = refreshTokenRepository.findByToken(refreshToken)
                .filter(rt -> rt.getExpiryDate().isAfter(Instant.now()))
                .orElse(null);
        if (tokenEntity == null) {
            return AuthResponse.builder().success(false).message("Invalid or expired refresh token").build();
        }
        User user = tokenEntity.getUser();
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        UUID organizationId = user.getOrganizationId();
        UUID departmentId = getDepartmentIdForUser(user.getId());
        UUID teamId = getTeamIdForUser(user.getId());
        
        // Generate new JWT token with complete user context
        String jwtToken = generateJwtToken(user, departmentId, teamId);
        // Optionally rotate refresh token
        refreshTokenRepository.delete(tokenEntity);
        RefreshToken newRefreshToken = createRefreshToken(user);
        setAuthCookies(response, jwtToken, newRefreshToken.getToken());
        
        return AuthResponse.builder()
                .success(true)
                .username(user.getUsername())
                .organizationId(organizationId)
                .roles(user.getRoles().stream().map(Role::getName).toList())
                .message("Token refreshed successfully")
                .build();
    }

    public RefreshToken createRefreshToken(User user) {
        String token = UUID.randomUUID().toString();
        Instant expiry = Instant.now().plusMillis(refreshExpiration);
        RefreshToken refreshToken = new RefreshToken(token, expiry, user);
        return refreshTokenRepository.save(refreshToken);
    }

    public boolean validateRefreshToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .filter(rt -> rt.getExpiryDate().isAfter(Instant.now()))
                .isPresent();
    }

    public void deleteRefreshToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshTokenRepository::delete);
    }

    private void setAuthCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        jakarta.servlet.http.Cookie accessCookie = new jakarta.servlet.http.Cookie("access_token", accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(60 * 15); // 15 min
        // Remove setSecure for development (HTTP)
        // accessCookie.setSecure(true);
        response.addCookie(accessCookie);

        jakarta.servlet.http.Cookie refreshCookie = new jakarta.servlet.http.Cookie("refresh_token", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/api/auth/refresh");
        refreshCookie.setMaxAge((int) (refreshExpiration / 1000));
        // Remove setSecure for development (HTTP)
        // refreshCookie.setSecure(true);
        response.addCookie(refreshCookie);
    }
}