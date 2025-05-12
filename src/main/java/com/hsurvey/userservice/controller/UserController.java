package com.hsurvey.userservice.controller;


import com.hsurvey.userservice.dto.UserDTO;
import com.hsurvey.userservice.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER_CREATE')")
    public UserDTO createUser(@RequestBody UserDTO userDTO) {
        return userService.createUser(userDTO);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER_READ')")
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }
}