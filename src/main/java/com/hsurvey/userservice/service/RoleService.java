package com.hsurvey.userservice.service;

import com.hsurvey.userservice.dto.RoleDTO;
import java.util.List;
import java.util.UUID;

public interface RoleService {
    RoleDTO createRole(RoleDTO roleDTO);
    List<RoleDTO> getAllRoles();
    RoleDTO getRoleById(UUID roleId);
    void deleteRole(UUID roleId);
    void addPermissionToRole(UUID roleId, UUID permissionId);
    void removePermissionFromRole(UUID roleId, UUID permissionId);
}