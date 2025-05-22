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
    @PreAuthorize("hasAnyAuthority('PERMISSION_CREATE','ADMIN_ROOT')")
    public PermissionDTO createPermission(@RequestBody PermissionDTO permissionDTO) {
        return permissionService.createPermission(permissionDTO);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('PERMISSION_READ','ADMIN_ROOT')")
    public List<PermissionDTO> getAllPermissions() {
        return permissionService.getAllPermissions();
    }

    @GetMapping("/{permissionId}")
    @PreAuthorize("hasAnyAuthority('PERMISSION_READ','ADMIN_ROOT')")
    public PermissionDTO getPermissionById(@PathVariable Long permissionId) {
        return permissionService.getPermissionById(permissionId);
    }

    @PutMapping
    @PreAuthorize("hasAnyAuthority('PERMISSION_UPDATE','ADMIN_ROOT')")
    public PermissionDTO updatePermission(@RequestBody PermissionDTO permissionDTO) {
        return permissionService.updatePermission(permissionDTO);
    }

    @DeleteMapping("/{permissionId}")
    @PreAuthorize("hasAnyAuthority('PERMISSION_DELETE','ADMIN_ROOT')")
    public void deletePermission(@PathVariable Long permissionId) {
        permissionService.deletePermission(permissionId);
    }
}