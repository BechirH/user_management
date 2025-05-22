package com.hsurvey.userservice.service;

import com.hsurvey.userservice.dto.PermissionDTO;
import java.util.List;

public interface PermissionService {
    PermissionDTO createPermission(PermissionDTO permissionDTO);
    PermissionDTO getPermissionById(Long permissionId);
    List<PermissionDTO> getAllPermissions();
    PermissionDTO updatePermission(PermissionDTO permissionDTO);
    void deletePermission(Long permissionId);
}