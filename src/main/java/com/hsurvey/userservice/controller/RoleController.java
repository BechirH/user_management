package com.hsurvey.userservice.controller;

import com.hsurvey.userservice.dto.RoleDTO;
import com.hsurvey.userservice.service.RoleService;
import com.hsurvey.userservice.utils.OrganizationContextUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/roles")
public class RoleController {
    private final RoleService roleService;
    private final OrganizationContextUtil organizationContextUtil;

    public RoleController(RoleService roleService, OrganizationContextUtil organizationContextUtil) {
        this.roleService = roleService;
        this.organizationContextUtil = organizationContextUtil;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_CREATE','SYS_ADMIN_ROOT')")
    public ResponseEntity<RoleDTO> createRole(@RequestBody RoleDTO roleDTO) {
        UUID organizationId = organizationContextUtil.getCurrentOrganizationId();
        roleDTO.setOrganizationId(organizationId);
        RoleDTO createdRole = roleService.createRole(roleDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRole);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_READ','SYS_ADMIN_ROOT')")
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        List<RoleDTO> roles;

        if (organizationContextUtil.isRootAdmin()) {

            roles = roleService.getAllRoles();
        } else {

            UUID organizationId = organizationContextUtil.getCurrentOrganizationId();
            roles = roleService.getAllRolesByOrganization(organizationId);
        }

        return ResponseEntity.ok(roles);
    }

    @GetMapping("/{roleId}")
    @PreAuthorize("hasAnyAuthority('ROLE_READ','SYS_ADMIN_ROOT')")
    public ResponseEntity<RoleDTO> getRoleById(@PathVariable UUID roleId) {
        UUID organizationId = organizationContextUtil.getCurrentOrganizationId();
        RoleDTO role = roleService.getRoleByIdAndOrganization(roleId, organizationId);
        return ResponseEntity.ok(role);
    }

    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasAnyAuthority('ROLE_DELETE','SYS_ADMIN_ROOT')")
    public ResponseEntity<Void> deleteRole(@PathVariable UUID roleId) {
        UUID organizationId = organizationContextUtil.getCurrentOrganizationId();
        roleService.deleteRoleByIdAndOrganization(roleId, organizationId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasAnyAuthority('ROLE_UPDATE','SYS_ADMIN_ROOT')")
    public ResponseEntity<Void> addPermissionToRole(@PathVariable UUID roleId, @PathVariable UUID permissionId) {
        UUID organizationId = organizationContextUtil.getCurrentOrganizationId();
        roleService.addPermissionToRoleInOrganization(roleId, permissionId, organizationId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasAnyAuthority('ROLE_UPDATE','SYS_ADMIN_ROOT')")
    public ResponseEntity<Void> removePermissionFromRole(@PathVariable UUID roleId, @PathVariable UUID permissionId) {
        UUID organizationId = organizationContextUtil.getCurrentOrganizationId();
        roleService.removePermissionFromRoleInOrganization(roleId, permissionId, organizationId);
        return ResponseEntity.noContent().build();
    }
}