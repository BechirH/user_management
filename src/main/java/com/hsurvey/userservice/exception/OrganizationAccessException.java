package com.hsurvey.userservice.exception;

public class OrganizationAccessException extends RuntimeException {
    public OrganizationAccessException(String message) {
        super(message);
    }

    public OrganizationAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}