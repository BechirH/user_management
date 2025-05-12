package com.hsurvey.userservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.util.Set;

@Data
public class UserDTO {
    private Long id;
    private String username;
    @JsonIgnore // Prevent password from appearing in responses
    private String password;
    private Set<String> roles;
}
