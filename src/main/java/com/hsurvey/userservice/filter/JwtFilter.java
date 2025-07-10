package com.hsurvey.userservice.filter;

import com.hsurvey.userservice.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println("Incoming request path: " + request.getRequestURI());
        String path = request.getRequestURI();
        if (path.startsWith("/api/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = null;
        final String authorizationHeader = request.getHeader("Authorization");
        System.out.println("Authorization header: " + (authorizationHeader != null ? "present" : "null"));
        
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
            System.out.println("Token found in Authorization header");
        } else if (request.getCookies() != null) {
            System.out.println("Cookies found: " + request.getCookies().length);
            for (Cookie cookie : request.getCookies()) {
                System.out.println("Cookie: " + cookie.getName() + " = " + (cookie.getValue() != null ? "present" : "null"));
                if ("access_token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    System.out.println("Token found in access_token cookie");
                    break;
                }
            }
        } else {
            System.out.println("No cookies found");
        }

        if (token == null) {
            System.out.println("No token found, proceeding without authentication");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final Claims claims = jwtUtil.getAllClaimsFromToken(token);
            final String username = jwtUtil.extractUsername(token);
            System.out.println("Token validated for user: " + username);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                List<String> authorities = claims.get("authorities", List.class);
                List<GrantedAuthority> grantedAuthorities = authorities.stream()
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
                System.out.println("Authentication set for user: " + username);
            }
        } catch (Exception e) {
            System.out.println("Token validation failed: " + e.getMessage());
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
