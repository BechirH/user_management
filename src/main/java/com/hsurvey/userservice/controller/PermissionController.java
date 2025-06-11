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
    @PreAuthorize("hasAnyAuthority('PERMISSION_CREATE','SYS_ADMIN_ROOT')")
    public ResponseEntity<PermissionDTO> createPermission(@RequestBody PermissionDTO permissionDTO) {
        PermissionDTO createdPermission;

        if (organizationContextUtil.isRootAdmin()) {

            if (permissionDTO.getOrganizationId() == null) {
                throw new IllegalArgumentException("Organization ID is required for sys admin permission creation");
            }
            createdPermission = permissionService.createPermissionForOrganization(permissionDTO, permissionDTO.getOrganizationId());
        } else {

            UUID organizationId = organizationContextUtil.getCurrentOrganizationId();
            permissionDTO.setOrganizationId(organizationId);
            createdPermission = permissionService.createPermission(permissionDTO);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(createdPermission);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('PERMISSION_READ','SYS_ADMIN_ROOT')")
    public ResponseEntity<List<PermissionDTO>> getAllPermissions() {
        List<PermissionDTO> permissions;

        if (organizationContextUtil.isRootAdmin()) {
            permissions = permissionService.getAllPermissions();
        } else {
            UUID organizationId = organizationContextUtil.getCurrentOrganizationId();
            permissions = permissionService.getAllPermissionsByOrganization(organizationId);
        }

        return ResponseEntity.ok(permissions);
    }

    @GetMapping("/{permissionId}")
    @PreAuthorize("hasAnyAuthority('PERMISSION_READ','SYS_ADMIN_ROOT')")
    public ResponseEntity<PermissionDTO> getPermissionById(@PathVariable UUID permissionId) {
        PermissionDTO permission;

        if (organizationContextUtil.isRootAdmin()) {
            permission = permissionService.getPermissionById(permissionId);
        } else {
            UUID organizationId = organizationContextUtil.getCurrentOrganizationId();
            permission = permissionService.getPermissionByIdAndOrganization(permissionId, organizationId);
        }

        return ResponseEntity.ok(permission);
    }

    @PutMapping("/{permissionId}")
    @PreAuthorize("hasAnyAuthority('PERMISSION_UPDATE','SYS_ADMIN_ROOT')")
    public ResponseEntity<PermissionDTO> updatePermission(
            @PathVariable UUID permissionId,
            @RequestBody PermissionDTO permissionDTO) {
        PermissionDTO updatedPermission;

        if (organizationContextUtil.isRootAdmin()) {
            updatedPermission = permissionService.updatePermission(permissionId, permissionDTO);
        } else {
            UUID organizationId = organizationContextUtil.getCurrentOrganizationId();
            updatedPermission = permissionService.updatePermissionInOrganization(permissionId, permissionDTO, organizationId);
        }

        return ResponseEntity.ok(updatedPermission);
    }

    @DeleteMapping("/{permissionId}")
    @PreAuthorize("hasAnyAuthority('PERMISSION_DELETE','SYS_ADMIN_ROOT')")
    public ResponseEntity<Void> deletePermission(@PathVariable UUID permissionId) {
        if (organizationContextUtil.isRootAdmin()) {
            permissionService.deletePermissionById(permissionId);
        } else {
            UUID organizationId = organizationContextUtil.getCurrentOrganizationId();
            permissionService.deletePermissionByIdAndOrganization(permissionId, organizationId);
        }

        return ResponseEntity.noContent().build();
    }
}