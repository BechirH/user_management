package com.hsurvey.userservice.service;

import com.hsurvey.userservice.dto.RoleDTO;
import java.util.List;
import java.util.UUID;

public interface RoleService {
    // Create methods
    RoleDTO createRole(RoleDTO roleDTO);
    RoleDTO createRoleForOrganization(RoleDTO roleDTO, UUID targetOrganizationId);

    // Read methods
    List<RoleDTO> getAllRoles();
    List<RoleDTO> getAllRolesByOrganization(UUID organizationId);
    RoleDTO getRoleById(UUID roleId);
    RoleDTO getRoleByIdAndOrganization(UUID roleId, UUID organizationId);

    // Update methods
    RoleDTO updateRole(UUID roleId, RoleDTO roleDTO);
    RoleDTO updateRoleInOrganization(UUID roleId, RoleDTO roleDTO, UUID organizationId);

    // Delete methods
    void deleteRoleById(UUID roleId);
    void deleteRoleByIdAndOrganization(UUID roleId, UUID organizationId);

    // Permission management methods
    void addPermissionToRole(UUID roleId, UUID permissionId);
    void addPermissionToRoleInOrganization(UUID roleId, UUID permissionId, UUID organizationId);
    void removePermissionFromRole(UUID roleId, UUID permissionId);
    void removePermissionFromRoleInOrganization(UUID roleId, UUID permissionId, UUID organizationId);
}