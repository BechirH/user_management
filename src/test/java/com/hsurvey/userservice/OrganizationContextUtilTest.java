package com.hsurvey.userservice;

import com.hsurvey.userservice.utils.OrganizationContextUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OrganizationContextUtilTest {

    private OrganizationContextUtil organizationContextUtil;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        organizationContextUtil = new OrganizationContextUtil();
        request = new MockHttpServletRequest();
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);
    }

    @Test
    void testExtractOrganizationIdFromHeader() {
        // Given
        UUID expectedOrgId = UUID.randomUUID();
        request.addHeader("X-Organization-Id", expectedOrgId.toString());

        // When
        UUID result = organizationContextUtil.getCurrentOrganizationId();

        // Then
        assertEquals(expectedOrgId, result);
    }

    @Test
    void testNoOrganizationIdHeader() {
        // Given
        // No X-Organization-Id header

        // When & Then
        assertThrows(SecurityException.class, () -> {
            organizationContextUtil.getCurrentOrganizationId();
        });
    }

    @Test
    void testInvalidOrganizationIdFormat() {
        // Given
        request.addHeader("X-Organization-Id", "invalid-uuid-format");

        // When & Then
        assertThrows(SecurityException.class, () -> {
            organizationContextUtil.getCurrentOrganizationId();
        });
    }

    @Test
    void testEmptyOrganizationIdHeader() {
        // Given
        request.addHeader("X-Organization-Id", "");

        // When & Then
        assertThrows(SecurityException.class, () -> {
            organizationContextUtil.getCurrentOrganizationId();
        });
    }
} 