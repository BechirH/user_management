package com.hsurvey.userservice.service;

import com.hsurvey.userservice.dto.RoleDTO;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleService {
    RoleDTO createRole(RoleDTO roleDTO);
    List<RoleDTO> getAllRoles();
    List<RoleDTO> getAllRolesByOrganization(UUID organizationId);
    RoleDTO getRoleById(UUID roleId);
    RoleDTO getRoleByNameAndOrganization(String roleName, UUID organizationId);
    RoleDTO updateRole(UUID roleId, RoleDTO roleDTO);
    void deleteRole(UUID roleId);
    void addPermissionToRole(UUID roleId, UUID permissionId);
    void removePermissionFromRole(UUID roleId, UUID permissionId);
    void addPermissionsToRole(UUID roleId, List<UUID> permissionIds);
    void removePermissionsFromRole(UUID roleId, List<UUID> permissionIds);
    boolean existsByNameAndOrganization(String roleName, UUID organizationId);
    Optional<RoleDTO> findByNameAndOrganization(String roleName, UUID organizationId);
    RoleDTO getRoleByIdAndOrganization(UUID roleId, UUID organizationId);
    void deleteRoleByIdAndOrganization(UUID roleId, UUID organizationId);
    void addPermissionToRoleInOrganization(UUID roleId, UUID permissionId, UUID organizationId);
    void removePermissionFromRoleInOrganization(UUID roleId, UUID permissionId, UUID organizationId);
}