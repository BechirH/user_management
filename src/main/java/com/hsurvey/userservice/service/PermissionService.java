package com.hsurvey.userservice.service;

import com.hsurvey.userservice.dto.PermissionDTO;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermissionService {
    PermissionDTO createPermission(PermissionDTO permissionDTO);
    PermissionDTO getPermissionById(UUID permissionId);
    List<PermissionDTO> getAllPermissions();
    List<PermissionDTO> getAllPermissionsByOrganization(UUID organizationId);
    PermissionDTO getPermissionByNameAndOrganization(String permissionName, UUID organizationId);
    PermissionDTO updatePermission(UUID permissionId, PermissionDTO permissionDTO);
    void deletePermission(UUID permissionId);
    boolean existsByNameAndOrganization(String permissionName, UUID organizationId);
    Optional<PermissionDTO> findByNameAndOrganization(String permissionName, UUID organizationId);
    List<PermissionDTO> createPermissionsForOrganization(UUID organizationId, List<String> permissionNames);
    List<PermissionDTO> getPermissionsByIds(List<UUID> permissionIds);
    long countPermissionsByOrganization(UUID organizationId);
    void deletePermissionsByOrganization(UUID organizationId);
    PermissionDTO getPermissionByIdAndOrganization(UUID permissionId, UUID organizationId);
    PermissionDTO updatePermissionInOrganization(UUID permissionId, PermissionDTO permissionDTO, UUID organizationId);
    void deletePermissionByIdAndOrganization(UUID permissionId, UUID organizationId);
}