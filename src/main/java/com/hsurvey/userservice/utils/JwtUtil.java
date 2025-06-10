package com.hsurvey.userservice.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtUtil {
    private final String secret;
    private final long expiration;
    private final Key signingKey;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration}") long expiration) {
        this.secret = secret;
        this.expiration = expiration;
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    // UPDATED: Add organizationId parameter
    public String generateToken(UserDetails userDetails, UUID organizationId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        // ADD: Organization ID to token claims
        if (organizationId != null) {
            claims.put("organizationId", organizationId.toString());
        }

        return createToken(claims, userDetails.getUsername());
    }

    // KEEP: Original method for backward compatibility
    public String generateToken(UserDetails userDetails) {
        return generateToken(userDetails, null);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // NEW: Extract organization ID from token
    public UUID extractOrganizationId(String token) {
        Claims claims = getAllClaimsFromToken(token);
        String orgId = (String) claims.get("organizationId");
        return orgId != null ? UUID.fromString(orgId) : null;
    }

    // NEW: Extract authorities from token
    @SuppressWarnings("unchecked")
    public List<String> extractAuthorities(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return (List<String>) claims.get("authorities");
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}