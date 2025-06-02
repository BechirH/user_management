package com.hsurvey.userservice.service;

import com.hsurvey.userservice.dto.PermissionDTO;
import java.util.List;
import java.util.UUID;

public interface PermissionService {
    PermissionDTO createPermission(PermissionDTO permissionDTO);
    PermissionDTO getPermissionById(UUID permissionId);
    List<PermissionDTO> getAllPermissions();
    PermissionDTO updatePermission(PermissionDTO permissionDTO);
    void deletePermission(UUID permissionId);
}