package com.hsurvey.userservice.exception;

public class OrganizationManagerAlreadyExistsException extends RuntimeException {
    public OrganizationManagerAlreadyExistsException(String message) {
        super(message);
    }
} 