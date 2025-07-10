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

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

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
}