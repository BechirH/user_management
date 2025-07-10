package com.hsurvey.userservice;

import com.hsurvey.userservice.utils.OrganizationContextUtil;
import com.hsurvey.userservice.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizationContextUtilTest {

    @Mock
    private JwtUtil jwtUtil;

    private OrganizationContextUtil organizationContextUtil;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        organizationContextUtil = new OrganizationContextUtil(jwtUtil);
        request = new MockHttpServletRequest();
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);
    }

    @Test
    void testExtractTokenFromAuthorizationHeader() {
        // Given
        String token = "test.jwt.token";
        request.addHeader("Authorization", "Bearer " + token);
        UUID expectedOrgId = UUID.randomUUID();
        when(jwtUtil.extractOrganizationId(token)).thenReturn(expectedOrgId);

        // When
        UUID result = organizationContextUtil.getCurrentOrganizationId();

        // Then
        assertEquals(expectedOrgId, result);
    }

    @Test
    void testExtractTokenFromCookie() {
        // Given
        String token = "test.jwt.token";
        request.addHeader("Authorization", ""); // No Authorization header
        request.setCookies(new jakarta.servlet.http.Cookie("access_token", token));
        UUID expectedOrgId = UUID.randomUUID();
        when(jwtUtil.extractOrganizationId(token)).thenReturn(expectedOrgId);

        // When
        UUID result = organizationContextUtil.getCurrentOrganizationId();

        // Then
        assertEquals(expectedOrgId, result);
    }

    @Test
    void testNoTokenFound() {
        // Given
        request.addHeader("Authorization", ""); // No Authorization header
        request.setCookies(); // No cookies

        // When & Then
        assertThrows(SecurityException.class, () -> {
            organizationContextUtil.getCurrentOrganizationId();
        });
    }

    @Test
    void testAuthorizationHeaderTakesPrecedence() {
        // Given
        String headerToken = "header.jwt.token";
        String cookieToken = "cookie.jwt.token";
        request.addHeader("Authorization", "Bearer " + headerToken);
        request.setCookies(new jakarta.servlet.http.Cookie("access_token", cookieToken));
        UUID expectedOrgId = UUID.randomUUID();
        when(jwtUtil.extractOrganizationId(headerToken)).thenReturn(expectedOrgId);

        // When
        UUID result = organizationContextUtil.getCurrentOrganizationId();

        // Then
        assertEquals(expectedOrgId, result);
    }
} 