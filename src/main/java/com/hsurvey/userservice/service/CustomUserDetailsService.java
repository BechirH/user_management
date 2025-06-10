package com.hsurvey.userservice.service;

import com.hsurvey.userservice.entities.User;
import com.hsurvey.userservice.repositories.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(getAuthorities(user))
                .build();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        // Handle null roles
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return user.getRoles().stream()
                .flatMap(role -> {
                    // Handle null permissions
                    if (role.getPermissions() == null || role.getPermissions().isEmpty()) {
                        return List.of(new SimpleGrantedAuthority("ROLE_" + role.getName())).stream();
                    }
                    return role.getPermissions().stream()
                            .map(permission -> new SimpleGrantedAuthority(permission.getName()));
                })
                .collect(Collectors.toList());
    }
}