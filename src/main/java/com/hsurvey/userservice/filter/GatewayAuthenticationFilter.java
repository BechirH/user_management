package com.hsurvey.userservice.filter;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GatewayAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        if (path.startsWith("/api/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Check if request is authenticated by gateway
        String authenticated = request.getHeader("X-Authenticated");
        if (!"true".equals(authenticated)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Request not authenticated by gateway");
            return;
        }

        try {
            // Extract user information from gateway headers
            String username = request.getHeader("X-Username");
            String userId = request.getHeader("X-User-Id");
            String organizationId = request.getHeader("X-Organization-Id");
            String authoritiesHeader = request.getHeader("X-Authorities");

            if (username == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No user information found");
                return;
            }

            // Convert authorities string to GrantedAuthority list
            List<GrantedAuthority> grantedAuthorities = Arrays.stream(authoritiesHeader.split(","))
                    .filter(auth -> !auth.trim().isEmpty())
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            grantedAuthorities);

            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error processing authentication");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
