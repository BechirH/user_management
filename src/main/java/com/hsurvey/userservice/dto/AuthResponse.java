package com.hsurvey.userservice.dto;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private boolean success;
    private String token;
    private String username;
    private String message;
    private UUID organizationId;
}