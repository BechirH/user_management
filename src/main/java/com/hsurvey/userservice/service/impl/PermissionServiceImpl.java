package com.hsurvey.userservice.service.impl;

import com.hsurvey.userservice.dto.PermissionDTO;
import com.hsurvey.userservice.entities.Permission;
import com.hsurvey.userservice.entities.Role;
import com.hsurvey.userservice.mapper.PermissionMapper;
import com.hsurvey.userservice.repositories.PermissionRepository;
import com.hsurvey.userservice.repositories.RoleRepository;
import com.hsurvey.userservice.service.PermissionService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.hsurvey.userservice.annotation.RequireOrganizationAccess;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl implements PermissionService {
    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;
    private final RoleRepository roleRepository;

    public PermissionServiceImpl(PermissionRepository permissionRepository,
                                 PermissionMapper permissionMapper,
                                 RoleRepository roleRepository) {

        this.permissionRepository = permissionRepository;
        this.permissionMapper = permissionMapper;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public PermissionDTO createPermission(PermissionDTO permissionDTO) {
        // Validate that organizationId is provided
        if (permissionDTO.getOrganizationId() == null) {
            throw new IllegalArgumentException("Organization ID is required");
        }
        if (permissionDTO.getName().startsWith("SYS_")) {
            throw new SecurityException("Cannot create permissions with SYS_ prefix - reserved for system authorities");
        }
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
    @RequireOrganizationAccess(organizationIdParam = "organizationId")
    public PermissionDTO getPermissionByIdAndOrganization(UUID permissionId, UUID organizationId) {
        if (permissionId == null) {
            throw new IllegalArgumentException("Permission ID cannot be null");
        }
        if (organizationId == null) {
            throw new IllegalArgumentException("Organization ID cannot be null");
        }

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new EntityNotFoundException("Permission not found with id: " + permissionId));

        // Verify the permission belongs to the specified organization
        if (!permission.getOrganizationId().equals(organizationId)) {
            throw new EntityNotFoundException("Permission not found in the specified organization");
        }

        return permissionMapper.toDto(permission);
    }

    @Override
    @Transactional
    @RequireOrganizationAccess(organizationIdParam = "organizationId")
    public PermissionDTO updatePermissionInOrganization(UUID permissionId, PermissionDTO permissionDTO, UUID organizationId) {
        if (permissionId == null) {
            throw new IllegalArgumentException("Permission ID cannot be null");
        }
        if (organizationId == null) {
            throw new IllegalArgumentException("Organization ID cannot be null");
        }
        if (permissionDTO == null) {
            throw new IllegalArgumentException("Permission data cannot be null");
        }

        Permission existingPermission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new EntityNotFoundException("Permission not found with id: " + permissionId));

        // Verify the permission belongs to the specified organization
        if (!existingPermission.getOrganizationId().equals(organizationId)) {
            throw new EntityNotFoundException("Permission not found in the specified organization");
        }

        // Prevent changing organization ID
        if (permissionDTO.getOrganizationId() != null &&
                !existingPermission.getOrganizationId().equals(permissionDTO.getOrganizationId())) {
            throw new IllegalArgumentException("Cannot change organization ID of existing permission");
        }

        // Check for name conflicts within the organization
        if (!existingPermission.getName().equals(permissionDTO.getName()) &&
                permissionRepository.existsByNameAndOrganizationId(
                        permissionDTO.getName(), existingPermission.getOrganizationId())) {
            throw new IllegalArgumentException("Permission with name '" + permissionDTO.getName() +
                    "' already exists in this organization");
        }

        // Update fields
        existingPermission.setName(permissionDTO.getName());
        existingPermission.setDescription(permissionDTO.getDescription());

        Permission updatedPermission = permissionRepository.save(existingPermission);
        return permissionMapper.toDto(updatedPermission);
    }



    @Override
    @Transactional
    @RequireOrganizationAccess(organizationIdParam = "organizationId")
    public void deletePermissionByIdAndOrganization(UUID permissionId, UUID organizationId) {
        if (permissionId == null) {
            throw new IllegalArgumentException("Permission ID cannot be null");
        }
        if (organizationId == null) {
            throw new IllegalArgumentException("Organization ID cannot be null");
        }

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new EntityNotFoundException("Permission not found with id: " + permissionId));

        if (!permission.getOrganizationId().equals(organizationId)) {
            throw new EntityNotFoundException("Permission not found in the specified organization");
        }

        // Remove this permission from all roles that have it
        List<Role> rolesWithPermission = roleRepository.findByPermissionsContaining(permission);
        for (Role role : rolesWithPermission) {
            role.getPermissions().remove(permission);
            roleRepository.save(role);
        }

        // Now safe to delete the permission
        permissionRepository.delete(permission);
    }
}