package com.hsurvey.userservice.service;

import com.hsurvey.userservice.dto.AuthRequest;
import com.hsurvey.userservice.dto.AuthResponse;
import com.hsurvey.userservice.dto.RegisterRequest;
import com.hsurvey.userservice.dto.AdminRegisterRequest;

import jakarta.servlet.http.HttpServletResponse;

import java.util.UUID;

public interface AuthService {
    AuthResponse register(RegisterRequest request, HttpServletResponse response);
    AuthResponse registerAdmin(AdminRegisterRequest request, UUID organizationId, HttpServletResponse response);
    AuthResponse authenticate(AuthRequest request, HttpServletResponse response);

}