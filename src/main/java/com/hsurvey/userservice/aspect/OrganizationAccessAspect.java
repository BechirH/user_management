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

        if (requireOrganizationAccess.allowRootAdmin() && organizationContextUtil.isRootAdmin()) {
            logger.debug("Root admin access - bypassing organization validation");
            return;
        }
        UUID targetOrganizationId = extractOrganizationId(joinPoint, requireOrganizationAccess.organizationIdParam());

        if (targetOrganizationId == null) {
            logger.error("Organization ID parameter '{}' not found in method: {}",
                    requireOrganizationAccess.organizationIdParam(), joinPoint.getSignature().getName());
            throw new IllegalArgumentException("Organization ID parameter not found or is null");
        }
        UUID currentOrganizationId;
        try {
            currentOrganizationId = organizationContextUtil.getCurrentOrganizationId();
        } catch (SecurityException e) {
            logger.error("Failed to get current organization ID", e);
            throw new OrganizationAccessException("No valid organization context found");
        }

        if (!currentOrganizationId.equals(targetOrganizationId)) {
            logger.warn("Organization access denied. Current: {}, Requested: {}",
                    currentOrganizationId, targetOrganizationId);
            throw new OrganizationAccessException(requireOrganizationAccess.message());
        }

        logger.debug("Organization access validated successfully for organization: {}", targetOrganizationId);
    }
    private UUID extractOrganizationId(JoinPoint joinPoint, String parameterName) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Parameter[] parameters = signature.getMethod().getParameters();
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getName().equals(parameterName)) {
                Object arg = args[i];
                if (arg instanceof UUID) {
                    return (UUID) arg;
                }
                logger.warn("Parameter '{}' found but not of type UUID", parameterName);
            }
        }
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getType().equals(UUID.class)) {
                Object arg = args[i];
                if (arg instanceof UUID) {
                    logger.debug("Using UUID parameter at index {} as organizationId", i);
                    return (UUID) arg;
                }
            }
        }

        return null;
    }
}