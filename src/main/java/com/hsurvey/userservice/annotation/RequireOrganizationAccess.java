package com.hsurvey.userservice.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireOrganizationAccess {

    String organizationIdParam() default "organizationId";


    boolean allowRootAdmin() default true;

    String message() default "Access denied: insufficient organization permissions";
}