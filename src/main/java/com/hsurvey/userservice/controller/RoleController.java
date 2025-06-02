package com.hsurvey.userservice.controller;

import com.hsurvey.userservice.dto.RoleDTO;
import com.hsurvey.userservice.service.RoleService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/roles")
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_CREATE','ADMIN_ROOT')")
    public RoleDTO createRole(@RequestBody RoleDTO roleDTO) {
        return roleService.createRole(roleDTO);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_READ','ADMIN_ROOT')")
    public List<RoleDTO> getAllRoles() {
        return roleService.getAllRoles();
    }

    @GetMapping("/{roleId}")
    @PreAuthorize("hasAnyAuthority('ROLE_READ','ADMIN_ROOT')")
    public RoleDTO getRoleById(@PathVariable UUID roleId) {
        return roleService.getRoleById(roleId);
    }

    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasAnyAuthority('ROLE_DELETE','ADMIN_ROOT')")
    public void deleteRole(@PathVariable UUID roleId) {
        roleService.deleteRole(roleId);
    }

    @PostMapping("/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasAnyAuthority('ROLE_UPDATE','ADMIN_ROOT')")
    public void addPermissionToRole(@PathVariable UUID roleId, @PathVariable UUID permissionId) {
        roleService.addPermissionToRole(roleId, permissionId);
    }

    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasAnyAuthority('ROLE_UPDATE','ADMIN_ROOT')")
    public void removePermissionFromRole(@PathVariable UUID roleId, @PathVariable UUID permissionId) {
        roleService.removePermissionFromRole(roleId, permissionId);
    }
}