package com.hsurvey.userservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.util.Set;
import java.util.UUID;

@Data
public class UserDTO {
    private UUID id;
    private String username;
    private String email;
    @JsonIgnore
    private String password;
    private Set<String> roles;
    private UUID organizationId;
    private UUID departmentId;
    private UUID teamId;
}