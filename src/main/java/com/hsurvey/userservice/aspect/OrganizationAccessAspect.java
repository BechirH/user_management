package com.hsurvey.userservice.aspect;

import com.hsurvey.userservice.annotation.RequireOrganizationAccess;
import com.hsurvey.userservice.exception.OrganizationAccessException;
import com.hsurvey.userservice.utils.OrganizationContextUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Parameter;
import java.util.UUID;

@Aspect
@Component
public class OrganizationAccessAspect {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationAccessAspect.class);
    private final OrganizationContextUtil organizationContextUtil;

    public OrganizationAccessAspect(OrganizationContextUtil organizationContextUtil) {
        this.organizationContextUtil = organizationContextUtil;
    }

    @Before("@annotation(requireOrganizationAccess)")
    public void validateOrganizationAccess(JoinPoint joinPoint, RequireOrganizationAccess requireOrganizationAccess) {
        logger.debug("Validating organization access for method: {}", joinPoint.getSignature().getName());

        // Allow root admin to bypass validation if configured
        if (requireOrganizationAccess.allowRootAdmin() && organizationContextUtil.isRootAdmin()) {
            logger.debug("Root admin access - bypassing organization validation");
            return;
        }

        // Extract target organization ID from method parameters
        UUID targetOrganizationId = extractOrganizationId(joinPoint, requireOrganizationAccess.organizationIdParam());

        if (targetOrganizationId == null) {
            logger.error("Organization ID parameter '{}' not found or is null in method: {}",
                    requireOrganizationAccess.organizationIdParam(), joinPoint.getSignature().getName());
            throw new OrganizationAccessException("Organization ID parameter is required but not found");
        }

        // Get current user's organization context
        UUID currentOrganizationId;
        try {
            currentOrganizationId = organizationContextUtil.getCurrentOrganizationId();
        } catch (SecurityException e) {
            logger.error("Failed to get current organization ID: {}", e.getMessage());
            throw new OrganizationAccessException("No valid organization context found");
        }

        // Validate organization access
        if (!currentOrganizationId.equals(targetOrganizationId)) {
            logger.warn("Organization access denied. Current: {}, Requested: {}, Method: {}",
                    currentOrganizationId, targetOrganizationId, joinPoint.getSignature().getName());
            throw new OrganizationAccessException(requireOrganizationAccess.message());
        }

        logger.debug("Organization access validated successfully for organization: {}", targetOrganizationId);
    }

    private UUID extractOrganizationId(JoinPoint joinPoint, String parameterName) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Parameter[] parameters = signature.getMethod().getParameters();
        Object[] args = joinPoint.getArgs();

        // First, try to find parameter by exact name match
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (parameter.getName().equals(parameterName) && args[i] != null) {
                Object arg = args[i];
                if (arg instanceof UUID) {
                    logger.debug("Found organization ID by parameter name '{}': {}", parameterName, arg);
                    return (UUID) arg;
                } else {
                    logger.warn("Parameter '{}' found but not of type UUID, actual type: {}",
                            parameterName, arg.getClass().getSimpleName());
                }
            }
        }

        // Fallback: try to find any UUID parameter (for cases where parameter names aren't preserved)
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Object arg = args[i];
            if (parameter.getType().equals(UUID.class) && arg != null) {
                logger.debug("Fallback: Using UUID parameter at index {} as organizationId: {}", i, arg);
                return (UUID) arg;
            }
        }

        logger.error("No suitable organization ID parameter found. Expected parameter name: '{}', Available parameters: {}",
                parameterName, java.util.Arrays.toString(parameters));
        return null;
    }
}