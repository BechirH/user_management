package com.hsurvey.userservice.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;

}