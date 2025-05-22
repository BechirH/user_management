package com.hsurvey.userservice.controller;

import com.hsurvey.userservice.dto.UserDTO;
import com.hsurvey.userservice.service.UserService;
import org.springframework.http.ResponseEntity;
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
    @PreAuthorize("hasAnyAuthority('USER_CREATE', 'ADMIN_ROOT')")

    public UserDTO createUser(@RequestBody UserDTO userDTO) {
        return userService.createUser(userDTO);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('USER_READ','ADMIN_ROOT')")
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('USER_READ','ADMIN_ROOT')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO userDTO = userService.getUserById(id);
        return ResponseEntity.ok(userDTO);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('USER_UPDATE','ADMIN_ROOT')")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        UserDTO updatedUser = userService.updateUser(id, userDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('USER_DELETE','ADMIN_ROOT')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/roles/{roleId}")
    @PreAuthorize("hasAnyAuthority('USER_UPDATE','ADMIN_ROOT')")
    public ResponseEntity<UserDTO> addRoleToUser(
            @PathVariable Long userId,
            @PathVariable Long roleId) {
        UserDTO userDTO = userService.addRoleToUser(userId, roleId);
        return ResponseEntity.ok(userDTO);
    }

    @DeleteMapping("/{userId}/roles/{roleId}")
    @PreAuthorize("hasAnyAuthority('USER_UPDATE','ADMIN_ROOT')")
    public ResponseEntity<UserDTO> removeRoleFromUser(
            @PathVariable Long userId,
            @PathVariable Long roleId) {
        UserDTO userDTO = userService.removeRoleFromUser(userId, roleId);
        return ResponseEntity.ok(userDTO);
    }
}