package com.hsurvey.userservice.controller;

import com.hsurvey.userservice.dto.UserDTO;
import com.hsurvey.userservice.service.UserService;
import com.hsurvey.userservice.utils.OrganizationContextUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.hsurvey.userservice.dto.CreateUserDTO;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final OrganizationContextUtil organizationContextUtil;

    public UserController(UserService userService, OrganizationContextUtil organizationContextUtil) {
        this.userService = userService;
        this.organizationContextUtil = organizationContextUtil;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('USER_CREATE', 'SYS_ADMIN_ROOT')")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody CreateUserDTO createUserDTO) {
        UserDTO createdUser;

        if (organizationContextUtil.isRootAdmin()) {

            if (createUserDTO.getOrganizationId() == null) {
                throw new IllegalArgumentException("Organization ID is required for sys admin user creation");
            }
            createdUser = userService.createUserForOrganization(createUserDTO, createUserDTO.getOrganizationId());
        } else {

            UUID organizationId = organizationContextUtil.getCurrentOrganizationId();
            createUserDTO.setOrganizationId(organizationId);
            createdUser = userService.createUser(createUserDTO);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('USER_READ','SYS_ADMIN_ROOT')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users;

        if (organizationContextUtil.isRootAdmin()) {
            users = userService.getAllUsers();
        } else {
            UUID organizationId = organizationContextUtil.getCurrentOrganizationId();
            users = userService.getAllUsersByOrganization(organizationId);
        }

        return ResponseEntity.ok(users);
    }
    @GetMapping("/{userId}/exists")
    public ResponseEntity<Boolean> checkUserExists(@PathVariable UUID userId) {
        boolean exists = userService.existsById(userId);
        return ResponseEntity.ok(exists);
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('USER_READ','SYS_ADMIN_ROOT')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable UUID id) {
        UserDTO userDTO;

        if (organizationContextUtil.isRootAdmin()) {
            userDTO = userService.getUserById(id);
        } else {
            UUID organizationId = organizationContextUtil.getCurrentOrganizationId();
            userDTO = userService.getUserByIdAndOrganization(id, organizationId);
        }

        return ResponseEntity.ok(userDTO);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('USER_UPDATE','SYS_ADMIN_ROOT')")
    public ResponseEntity<UserDTO> updateUser(@PathVariable UUID id, @RequestBody UserDTO userDTO) {
        UserDTO updatedUser;

        if (organizationContextUtil.isRootAdmin()) {
            updatedUser = userService.updateUser(id, userDTO);
        } else {
            UUID organizationId = organizationContextUtil.getCurrentOrganizationId();
            updatedUser = userService.updateUserInOrganization(id, userDTO, organizationId);
        }

        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('USER_DELETE','SYS_ADMIN_ROOT')")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        if (organizationContextUtil.isRootAdmin()) {
            userService.deleteUserById(id);
        } else {
            UUID organizationId = organizationContextUtil.getCurrentOrganizationId();
            userService.deleteUserByIdAndOrganization(id, organizationId);
        }

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/roles/{roleId}")
    @PreAuthorize("hasAnyAuthority('USER_UPDATE','SYS_ADMIN_ROOT')")
    public ResponseEntity<UserDTO> addRoleToUser(
            @PathVariable UUID userId,
            @PathVariable UUID roleId) {
        UserDTO userDTO;

        if (organizationContextUtil.isRootAdmin()) {
            userDTO = userService.addRoleToUser(userId, roleId);
        } else {
            UUID organizationId = organizationContextUtil.getCurrentOrganizationId();
            userDTO = userService.addRoleToUserInOrganization(userId, roleId, organizationId);
        }

        return ResponseEntity.ok(userDTO);
    }

    @DeleteMapping("/{userId}/roles/{roleId}")
    @PreAuthorize("hasAnyAuthority('USER_UPDATE','SYS_ADMIN_ROOT')")
    public ResponseEntity<UserDTO> removeRoleFromUser(
            @PathVariable UUID userId,
            @PathVariable UUID roleId) {
        UserDTO userDTO;

        if (organizationContextUtil.isRootAdmin()) {
            userDTO = userService.removeRoleFromUser(userId, roleId);
        } else {
            UUID organizationId = organizationContextUtil.getCurrentOrganizationId();
            userDTO = userService.removeRoleFromUserInOrganization(userId, roleId, organizationId);
        }

        return ResponseEntity.ok(userDTO);
    }
}