package com.hsurvey.userservice.controller;


import com.hsurvey.userservice.dto.PermissionDTO;
import com.hsurvey.userservice.service.PermissionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {
    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSION_CREATE')")
    public PermissionDTO createPermission(@RequestBody PermissionDTO permissionDTO) {
        return permissionService.createPermission(permissionDTO);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSION_READ')")
    public List<PermissionDTO> getAllPermissions() {
        return permissionService.getAllPermissions();
    }
}