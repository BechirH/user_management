package com.hsurvey.userservice.service;

import com.hsurvey.userservice.dto.RoleDTO;
import java.util.List;

public interface RoleService {
    RoleDTO createRole(RoleDTO roleDTO);
    List<RoleDTO> getAllRoles();
    RoleDTO getRoleById(Long roleId);
    void deleteRole(Long roleId);
    void addPermissionToRole(Long roleId, Long permissionId);
    void removePermissionFromRole(Long roleId, Long permissionId); // Changed from String to Long
}