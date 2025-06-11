package com.hsurvey.userservice.service;

import com.hsurvey.userservice.dto.PermissionDTO;
import java.util.List;
import java.util.UUID;

public interface PermissionService {
    // Create methods
    PermissionDTO createPermission(PermissionDTO permissionDTO);
    PermissionDTO createPermissionForOrganization(PermissionDTO permissionDTO, UUID targetOrganizationId);

    // Read methods
    List<PermissionDTO> getAllPermissions();
    List<PermissionDTO> getAllPermissionsByOrganization(UUID organizationId);
    PermissionDTO getPermissionById(UUID permissionId);
    PermissionDTO getPermissionByIdAndOrganization(UUID permissionId, UUID organizationId);

    // Update methods
    PermissionDTO updatePermission(UUID permissionId, PermissionDTO permissionDTO);
    PermissionDTO updatePermissionInOrganization(UUID permissionId, PermissionDTO permissionDTO, UUID organizationId);

    // Delete methods
    void deletePermissionById(UUID permissionId);
    void deletePermissionByIdAndOrganization(UUID permissionId, UUID organizationId);
}