package com.hsurvey.userservice.controller;

import com.hsurvey.userservice.dto.PermissionDTO;
import com.hsurvey.userservice.service.PermissionService;
import com.hsurvey.userservice.utils.OrganizationContextUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {
    private final PermissionService permissionService;
    private final OrganizationContextUtil organizationContextUtil;

    public PermissionController(PermissionService permissionService, OrganizationContextUtil organizationContextUtil) {
        this.permissionService = permissionService;
        this.organizationContextUtil = organizationContextUtil;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('PERMISSION_CREATE','ADMIN_ROOT')")
    public ResponseEntity<PermissionDTO> createPermission(@RequestBody PermissionDTO permissionDTO) {
        UUID organizationId = organizationContextUtil.getCurrentOrganizationId();
        permissionDTO.setOrganizationId(organizationId); // Force organization from token
        PermissionDTO createdPermission = permissionService.createPermission(permissionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPermission);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('PERMISSION_READ','ADMIN_ROOT')")
    public ResponseEntity<List<PermissionDTO>> getAllPermissions() {
        List<PermissionDTO> permissions;

        if (organizationContextUtil.isRootAdmin()) {
            // Root admins can see all permissions
            permissions = permissionService.getAllPermissions();
        } else {
            // Regular users only see permissions from their organization
            UUID organizationId = organizationContextUtil.getCurrentOrganizationId();
            permissions = permissionService.getAllPermissionsByOrganization(organizationId);
        }

        return ResponseEntity.ok(permissions);
    }

    @GetMapping("/{permissionId}")
    @PreAuthorize("hasAnyAuthority('PERMISSION_READ','ADMIN_ROOT')")
    public ResponseEntity<PermissionDTO> getPermissionById(@PathVariable UUID permissionId) {
        UUID organizationId = organizationContextUtil.getCurrentOrganizationId();
        PermissionDTO permission = permissionService.getPermissionByIdAndOrganization(permissionId, organizationId);
        return ResponseEntity.ok(permission);
    }

    @PutMapping("/{permissionId}")
    @PreAuthorize("hasAnyAuthority('PERMISSION_UPDATE','ADMIN_ROOT')")
    public ResponseEntity<PermissionDTO> updatePermission(
            @PathVariable UUID permissionId,
            @RequestBody PermissionDTO permissionDTO) {
        UUID organizationId = organizationContextUtil.getCurrentOrganizationId();
        PermissionDTO updatedPermission = permissionService.updatePermissionInOrganization(permissionId, permissionDTO, organizationId);
        return ResponseEntity.ok(updatedPermission);
    }

    @DeleteMapping("/{permissionId}")
    @PreAuthorize("hasAnyAuthority('PERMISSION_DELETE','ADMIN_ROOT')")
    public ResponseEntity<Void> deletePermission(@PathVariable UUID permissionId) {
        UUID organizationId = organizationContextUtil.getCurrentOrganizationId();
        permissionService.deletePermissionByIdAndOrganization(permissionId, organizationId);
        return ResponseEntity.noContent().build();
    }
}