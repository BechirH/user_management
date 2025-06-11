package com.hsurvey.userservice.service;

import com.hsurvey.userservice.dto.AuthRequest;
import com.hsurvey.userservice.dto.AuthResponse;
import com.hsurvey.userservice.dto.RegisterRequest;
import com.hsurvey.userservice.dto.AdminRegisterRequest;

import java.util.UUID;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse registerAdmin(AdminRegisterRequest request, UUID organizationId);
    AuthResponse authenticate(AuthRequest request);
}