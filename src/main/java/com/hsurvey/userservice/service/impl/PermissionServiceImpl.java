package com.hsurvey.userservice.service.impl;

import com.hsurvey.userservice.dto.PermissionDTO;
import com.hsurvey.userservice.entities.Permission;
import com.hsurvey.userservice.mapper.PermissionMapper;
import com.hsurvey.userservice.repositories.PermissionRepository;
import com.hsurvey.userservice.service.PermissionService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.hsurvey.userservice.exception.OrganizationAccessException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl implements PermissionService {
    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    public PermissionServiceImpl(PermissionRepository permissionRepository,
                                 PermissionMapper permissionMapper) {
        this.permissionRepository = permissionRepository;
        this.permissionMapper = permissionMapper;
    }

    @Override
    @Transactional
    public PermissionDTO createPermission(PermissionDTO permissionDTO) {
        // Validate that organizationId is provided
        if (permissionDTO.getOrganizationId() == null) {
            throw new IllegalArgumentException("Organization ID is required");
        }

        // Check if permission with same name already exists in the organization
        if (permissionRepository.existsByNameAndOrganizationId(
                permissionDTO.getName(), permissionDTO.getOrganizationId())) {
            throw new IllegalArgumentException("Permission with name '" + permissionDTO.getName() +
                    "' already exists in this organization");
        }

        Permission permission = permissionMapper.toEntity(permissionDTO);
        Permission savedPermission = permissionRepository.save(permission);
        return permissionMapper.toDto(savedPermission);
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionDTO getPermissionById(UUID permissionId) {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new EntityNotFoundException("Permission not found with id: " + permissionId));
        return permissionMapper.toDto(permission);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionDTO> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(permissionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionDTO> getAllPermissionsByOrganization(UUID organizationId) {
        if (organizationId == null) {
            throw new IllegalArgumentException("Organization ID cannot be null");
        }

        return permissionRepository.findByOrganizationId(organizationId).stream()
                .map(permissionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionDTO getPermissionByNameAndOrganization(String permissionName, UUID organizationId) {
        if (permissionName == null || organizationId == null) {
            throw new IllegalArgumentException("Permission name and organization ID cannot be null");
        }

        return permissionRepository.findByNameAndOrganizationId(permissionName, organizationId)
                .map(permissionMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Permission not found with name: " + permissionName + " in organization: " + organizationId));
    }

    @Override
    @Transactional
    public PermissionDTO updatePermission(UUID permissionId, PermissionDTO permissionDTO) {
        Permission existingPermission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new EntityNotFoundException("Permission not found with id: " + permissionId));

        // Validate organization ID consistency
        if (permissionDTO.getOrganizationId() != null &&
                !existingPermission.getOrganizationId().equals(permissionDTO.getOrganizationId())) {
            throw new IllegalArgumentException("Cannot change organization ID of existing permission");
        }

        // Check if new name conflicts with existing permissions in the same organization
        if (!existingPermission.getName().equals(permissionDTO.getName()) &&
                permissionRepository.existsByNameAndOrganizationId(
                        permissionDTO.getName(), existingPermission.getOrganizationId())) {
            throw new IllegalArgumentException("Permission with name '" + permissionDTO.getName() +
                    "' already exists in this organization");
        }

        // Update permission properties
        existingPermission.setName(permissionDTO.getName());
        existingPermission.setDescription(permissionDTO.getDescription());

        Permission updatedPermission = permissionRepository.save(existingPermission);
        return permissionMapper.toDto(updatedPermission);
    }

    @Override
    @Transactional
    public void deletePermission(UUID permissionId) {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new EntityNotFoundException("Permission not found with id: " + permissionId));

        // Check if permission is being used by any roles
        // You might want to add a check here to prevent deletion of permissions in use

        permissionRepository.delete(permission);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNameAndOrganization(String permissionName, UUID organizationId) {
        if (permissionName == null || organizationId == null) {
            return false;
        }
        return permissionRepository.existsByNameAndOrganizationId(permissionName, organizationId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PermissionDTO> findByNameAndOrganization(String permissionName, UUID organizationId) {
        if (permissionName == null || organizationId == null) {
            return Optional.empty();
        }

        return permissionRepository.findByNameAndOrganizationId(permissionName, organizationId)
                .map(permissionMapper::toDto);
    }

    @Override
    @Transactional
    public List<PermissionDTO> createPermissionsForOrganization(UUID organizationId, List<String> permissionNames) {
        if (organizationId == null || permissionNames == null || permissionNames.isEmpty()) {
            throw new IllegalArgumentException("Organization ID and permission names are required");
        }

        return permissionNames.stream()
                .map(name -> {
                    // Check if permission already exists
                    Optional<Permission> existing = permissionRepository.findByNameAndOrganizationId(name, organizationId);
                    if (existing.isPresent()) {
                        return permissionMapper.toDto(existing.get());
                    }

                    // Create new permission
                    Permission permission = Permission.builder()
                            .name(name)
                            .organizationId(organizationId)
                            .description("Default " + name.toLowerCase().replace("_", " ") + " permission")
                            .build();

                    Permission savedPermission = permissionRepository.save(permission);
                    return permissionMapper.toDto(savedPermission);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionDTO> getPermissionsByIds(List<UUID> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return List.of();
        }

        List<Permission> permissions = permissionRepository.findAllById(permissionIds);
        return permissions.stream()
                .map(permissionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countPermissionsByOrganization(UUID organizationId) {
        if (organizationId == null) {
            return 0;
        }
        return permissionRepository.countByOrganizationId(organizationId);
    }

    @Override
    @Transactional
    public void deletePermissionsByOrganization(UUID organizationId) {
        if (organizationId == null) {
            throw new IllegalArgumentException("Organization ID cannot be null");
        }

        List<Permission> permissions = permissionRepository.findByOrganizationId(organizationId);
        permissionRepository.deleteAll(permissions);
    }
    @Override
    @Transactional(readOnly = true)
    public PermissionDTO getPermissionByIdAndOrganization(UUID permissionId, UUID organizationId) {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new EntityNotFoundException("Permission not found with id: " + permissionId));

        // Validate organization access
        if (!permission.getOrganizationId().equals(organizationId)) {
            throw new OrganizationAccessException("Access denied: Permission belongs to different organization");
        }

        return permissionMapper.toDto(permission);
    }

    @Override
    @Transactional
    public PermissionDTO updatePermissionInOrganization(UUID permissionId, PermissionDTO permissionDTO, UUID organizationId) {
        Permission existingPermission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new EntityNotFoundException("Permission not found with id: " + permissionId));

        // Validate organization access
        if (!existingPermission.getOrganizationId().equals(organizationId)) {
            throw new OrganizationAccessException("Access denied: Permission belongs to different organization");
        }

        // Validate organization ID consistency (prevent changing organization)
        if (permissionDTO.getOrganizationId() != null &&
                !existingPermission.getOrganizationId().equals(permissionDTO.getOrganizationId())) {
            throw new IllegalArgumentException("Cannot change organization ID of existing permission");
        }

        // Check if new name conflicts with existing permissions in the same organization
        if (!existingPermission.getName().equals(permissionDTO.getName()) &&
                permissionRepository.existsByNameAndOrganizationId(
                        permissionDTO.getName(), existingPermission.getOrganizationId())) {
            throw new IllegalArgumentException("Permission with name '" + permissionDTO.getName() +
                    "' already exists in this organization");
        }

        // Update permission properties
        existingPermission.setName(permissionDTO.getName());
        existingPermission.setDescription(permissionDTO.getDescription());

        Permission updatedPermission = permissionRepository.save(existingPermission);
        return permissionMapper.toDto(updatedPermission);
    }

    @Override
    @Transactional
    public void deletePermissionByIdAndOrganization(UUID permissionId, UUID organizationId) {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new EntityNotFoundException("Permission not found with id: " + permissionId));

        // Validate organization access
        if (!permission.getOrganizationId().equals(organizationId)) {
            throw new OrganizationAccessException("Access denied: Permission belongs to different organization");
        }

        // Check if permission is being used by any roles (optional - implement based on your needs)
        // long roleCount = rolePermissionRepository.countByPermissionId(permissionId);
        // if (roleCount > 0) {
        //     throw new IllegalStateException("Cannot delete permission that is assigned to roles");
        // }

        permissionRepository.delete(permission);
    }
}