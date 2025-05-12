package com.hsurvey.userservice.service;

import com.hsurvey.userservice.dto.AuthRequest;
import com.hsurvey.userservice.dto.AuthResponse;
import com.hsurvey.userservice.dto.RegisterRequest;
import com.hsurvey.userservice.entities.Role;
import com.hsurvey.userservice.entities.User;
import com.hsurvey.userservice.repositories.RoleRepository;
import com.hsurvey.userservice.repositories.UserRepository;
import com.hsurvey.userservice.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    public AuthResponse register(RegisterRequest request) {
        // Check if user exists
        if (userRepository.existsByUsername(request.getUsername())) {
            return AuthResponse.builder()
                    .message("Username already exists")
                    .build();
        }



        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        // Assign default role (ROLE_USER)
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role not found"));
        user.setRoles(Collections.singleton(userRole));

        // Save user
        userRepository.save(user);

        // Generate token using UserDetails
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String jwtToken = jwtUtil.generateToken(userDetails);

        return AuthResponse.builder()
                .token(jwtToken)
                .username(user.getUsername())
                .message("User registered successfully")
                .build();
    }

    public AuthResponse authenticate(AuthRequest request) {
        try {
            // Authenticate user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // Get user details
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());

            // Generate token
            String jwtToken = jwtUtil.generateToken(userDetails);

            return AuthResponse.builder()
                    .token(jwtToken)
                    .username(request.getUsername())
                    .message("Login successful")
                    .build();
        } catch (AuthenticationException e) {
            return AuthResponse.builder()
                    .message("Authentication failed: " + e.getMessage())
                    .build();
        }
    }
}