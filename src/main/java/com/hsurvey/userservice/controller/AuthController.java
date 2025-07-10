package com.hsurvey.userservice.controller;

import com.hsurvey.userservice.dto.AuthRequest;
import com.hsurvey.userservice.dto.AuthResponse;
import com.hsurvey.userservice.dto.RegisterRequest;
import com.hsurvey.userservice.dto.AdminRegisterRequest;
import com.hsurvey.userservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Cookie;
import com.hsurvey.userservice.utils.JwtUtil;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest, HttpServletResponse response) {
        return ResponseEntity.ok(authService.authenticate(authRequest, response));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(authService.register(request, response));
    }

    @PostMapping("/register/{organizationId}")
    public ResponseEntity<AuthResponse> registerAdmin(
            @PathVariable UUID organizationId,
            @Valid @RequestBody AdminRegisterRequest request,
            HttpServletResponse response) {
        return ResponseEntity.ok(authService.registerAdmin(request, organizationId, response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refresh_token".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }
        if (refreshToken == null) {
            return ResponseEntity.badRequest().body(AuthResponse.builder().success(false).message("Refresh token missing").build());
        }
        // Call a new method in AuthServiceImpl to handle refresh logic (to be implemented next)
        AuthResponse authResponse = ((com.hsurvey.userservice.service.impl.AuthServiceImpl)authService).refreshAccessToken(refreshToken, response);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response, HttpServletRequest request) {
        // Remove cookies
        jakarta.servlet.http.Cookie accessCookie = new jakarta.servlet.http.Cookie("access_token", "");
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(0);
        accessCookie.setSecure(true);
        response.addCookie(accessCookie);

        jakarta.servlet.http.Cookie refreshCookie = new jakarta.servlet.http.Cookie("refresh_token", "");
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/api/auth/refresh");
        refreshCookie.setMaxAge(0);
        refreshCookie.setSecure(true);
        response.addCookie(refreshCookie);

        // Optionally: Remove refresh token from DB
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("refresh_token".equals(cookie.getName())) {
                    ((com.hsurvey.userservice.service.impl.AuthServiceImpl)authService).deleteRefreshToken(cookie.getValue());
                }
            }
        }

        return ResponseEntity.ok().body("Logged out");
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser(HttpServletRequest request) {
        String token = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("access_token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        
        if (token == null) {
            return ResponseEntity.status(401).body(AuthResponse.builder()
                    .success(false)
                    .message("No access token found")
                    .build());
        }
        
        try {
            String username = jwtUtil.extractUsername(token);
            UUID userId = jwtUtil.extractUserId(token);
            UUID organizationId = jwtUtil.extractOrganizationId(token);
            List<String> authorities = jwtUtil.extractAuthorities(token);
            
            return ResponseEntity.ok(AuthResponse.builder()
                    .success(true)
                    .username(username)
                    .organizationId(organizationId)
                    .roles(authorities)
                    .message("Current user info retrieved")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(401).body(AuthResponse.builder()
                    .success(false)
                    .message("Invalid token")
                    .build());
        }
    }
}