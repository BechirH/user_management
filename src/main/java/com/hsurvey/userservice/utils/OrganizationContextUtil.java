package com.hsurvey.userservice.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collection;
import java.util.UUID;

@Component
public class OrganizationContextUtil {

    public UUID getCurrentOrganizationId() {
        String organizationId = extractHeaderValue("X-Organization-Id");
        if (organizationId == null || organizationId.trim().isEmpty()) {
            throw new SecurityException("No organization context found in request headers");
        }

        try {
            return UUID.fromString(organizationId);
        } catch (IllegalArgumentException e) {
            throw new SecurityException("Invalid organization ID format");
        }
    }


    public boolean isRootAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("SYS_ADMIN_ROOT"));
    }


    public void validateOrganizationAccess(UUID resourceOrganizationId) {
        if (resourceOrganizationId == null) {
            throw new IllegalArgumentException("Resource organization ID cannot be null");
        }

        // Root admins can access all organizations
        if (isRootAdmin()) {
            return;
        }

        UUID currentOrgId = getCurrentOrganizationId();
        if (!currentOrgId.equals(resourceOrganizationId)) {
            throw new SecurityException("Access denied: Resource belongs to different organization");
        }
    }


    public Collection<? extends GrantedAuthority> getCurrentAuthorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getAuthorities() : null;
    }


    private String extractHeaderValue(String headerName) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }

        HttpServletRequest request = attributes.getRequest();
        return request.getHeader(headerName);
                }

    public UUID getCurrentOrganizationIdOrNull() {
        try {
            return getCurrentOrganizationId();
        } catch (SecurityException e) {
            return null;
        }
    }

    public void validateOrganizationAccess(UUID resourceOrganizationId, String customErrorMessage) {
        if (resourceOrganizationId == null) {
            throw new IllegalArgumentException("Resource organization ID cannot be null");
        }

        if (isRootAdmin()) {
            return;
        }

        UUID currentOrgId = getCurrentOrganizationId();
        if (!currentOrgId.equals(resourceOrganizationId)) {
            throw new SecurityException(customErrorMessage != null ? customErrorMessage :
                    "Access denied: Resource belongs to different organization");
        }
    }

    public boolean hasOrganizationAccess(UUID organizationId) {
        if (organizationId == null) {
            return false;
        }

        try {
            if (isRootAdmin()) {
                return true;
            }

            UUID currentOrgId = getCurrentOrganizationId();
            return currentOrgId.equals(organizationId);
        } catch (SecurityException e) {
            return false;
        }
    }

    public boolean hasAuthority(String authority) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals(authority));
    }

    public UUID getCurrentDepartmentId() {
        String departmentId = extractHeaderValue("X-Department-Id");
        if (departmentId == null || departmentId.trim().isEmpty()) {
            return null;
        }

        try {
            return UUID.fromString(departmentId);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public UUID getCurrentTeamId() {
        String teamId = extractHeaderValue("X-Team-Id");
        if (teamId == null || teamId.trim().isEmpty()) {
            return null;
        }

        try {
            return UUID.fromString(teamId);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public boolean hasDepartmentId() {
        String departmentId = extractHeaderValue("X-Department-Id");
        return departmentId != null && !departmentId.trim().isEmpty();
    }

    public boolean hasTeamId() {
        String teamId = extractHeaderValue("X-Team-Id");
        return teamId != null && !teamId.trim().isEmpty();
    }
}