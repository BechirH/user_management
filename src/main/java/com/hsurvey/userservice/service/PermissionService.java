package com.hsurvey.userservice.service;

import com.hsurvey.userservice.dto.PermissionDTO;
import java.util.List;

import java.util.UUID;

public interface PermissionService {
    PermissionDTO createPermission(PermissionDTO permissionDTO);
    List<PermissionDTO> getAllPermissions();
    List<PermissionDTO> getAllPermissionsByOrganization(UUID organizationId);
    PermissionDTO getPermissionByIdAndOrganization(UUID permissionId, UUID organizationId);
    PermissionDTO updatePermissionInOrganization(UUID permissionId, PermissionDTO permissionDTO, UUID organizationId);
    void deletePermissionByIdAndOrganization(UUID permissionId, UUID organizationId);
}