package com.hsurvey.userservice.service;

import com.hsurvey.userservice.dto.RoleDTO;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleService {
    RoleDTO createRole(RoleDTO roleDTO);
    List<RoleDTO> getAllRoles();
    List<RoleDTO> getAllRolesByOrganization(UUID organizationId);

    RoleDTO getRoleByIdAndOrganization(UUID roleId, UUID organizationId);
    void deleteRoleByIdAndOrganization(UUID roleId, UUID organizationId);
    void addPermissionToRoleInOrganization(UUID roleId, UUID permissionId, UUID organizationId);
    void removePermissionFromRoleInOrganization(UUID roleId, UUID permissionId, UUID organizationId);
}