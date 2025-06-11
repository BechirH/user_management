package com.hsurvey.userservice.service.impl;

import com.hsurvey.userservice.dto.RoleDTO;
import com.hsurvey.userservice.entities.Permission;
import com.hsurvey.userservice.entities.Role;
import com.hsurvey.userservice.mapper.RoleMapper;
import com.hsurvey.userservice.repositories.PermissionRepository;
import com.hsurvey.userservice.repositories.RoleRepository;
import com.hsurvey.userservice.service.RoleService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.hsurvey.userservice.annotation.RequireOrganizationAccess;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;

    public RoleServiceImpl(RoleRepository roleRepository,
                           PermissionRepository permissionRepository,
                           RoleMapper roleMapper) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.roleMapper = roleMapper;
    }

    // CREATE METHODS
    @Override
    @Transactional
    public RoleDTO createRole(RoleDTO roleDTO) {
        return createRoleInternal(roleDTO, roleDTO.getOrganizationId());
    }

    @Override
    @Transactional
    public RoleDTO createRoleForOrganization(RoleDTO roleDTO, UUID targetOrganizationId) {
        return createRoleInternal(roleDTO, targetOrganizationId);
    }

    private RoleDTO createRoleInternal(RoleDTO roleDTO, UUID organizationId) {
        if (organizationId == null) {
            throw new IllegalArgumentException("Organization ID is required");
        }

        if (roleRepository.existsByNameAndOrganizationId(roleDTO.getName(), organizationId)) {
            throw new IllegalArgumentException("Role with name '" + roleDTO.getName() +
                    "' already exists in this organization");
        }


        roleDTO.setOrganizationId(organizationId);

        Role role = roleMapper.toEntity(roleDTO);
        if (role.getPermissions() == null) {
            role.setPermissions(new HashSet<>());
        }

        Role savedRole = roleRepository.save(role);
        return roleMapper.toDto(savedRole);
    }

    // READ METHODS
    @Override
    @Transactional(readOnly = true)
    public List<RoleDTO> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(roleMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleDTO> getAllRolesByOrganization(UUID organizationId) {
        if (organizationId == null) {
            throw new IllegalArgumentException("Organization ID cannot be null");
        }

        return roleRepository.findByOrganizationId(organizationId).stream()
                .map(roleMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDTO getRoleById(UUID roleId) {
        if (roleId == null) {
            throw new IllegalArgumentException("Role ID cannot be null");
        }

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + roleId));

        return roleMapper.toDto(role);
    }

    @Override
    @Transactional(readOnly = true)
    @RequireOrganizationAccess(organizationIdParam = "organizationId")
    public RoleDTO getRoleByIdAndOrganization(UUID roleId, UUID organizationId) {
        if (roleId == null) {
            throw new IllegalArgumentException("Role ID cannot be null");
        }
        if (organizationId == null) {
            throw new IllegalArgumentException("Organization ID cannot be null");
        }

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + roleId));


        if (!role.getOrganizationId().equals(organizationId)) {
            throw new EntityNotFoundException("Role not found in the specified organization");
        }

        return roleMapper.toDto(role);
    }

    // DELETE METHODS
    @Override
    @Transactional
    public void deleteRoleById(UUID roleId) {
        if (roleId == null) {
            throw new IllegalArgumentException("Role ID cannot be null");
        }

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + roleId));

        roleRepository.delete(role);
    }

    @Override
    @Transactional
    @RequireOrganizationAccess(organizationIdParam = "organizationId")
    public void deleteRoleByIdAndOrganization(UUID roleId, UUID organizationId) {
        if (roleId == null) {
            throw new IllegalArgumentException("Role ID cannot be null");
        }
        if (organizationId == null) {
            throw new IllegalArgumentException("Organization ID cannot be null");
        }

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + roleId));


        if (!role.getOrganizationId().equals(organizationId)) {
            throw new EntityNotFoundException("Role not found in the specified organization");
        }

        roleRepository.delete(role);
    }

    // PERMISSION MANAGEMENT METHODS
    @Override
    @Transactional
    public void addPermissionToRole(UUID roleId, UUID permissionId) {
        if (roleId == null) {
            throw new IllegalArgumentException("Role ID cannot be null");
        }
        if (permissionId == null) {
            throw new IllegalArgumentException("Permission ID cannot be null");
        }

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + roleId));

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new EntityNotFoundException("Permission not found with id: " + permissionId));


        if (role.getPermissions() == null) {
            role.setPermissions(new HashSet<>());
        }


        if (role.getPermissions().add(permission)) {
            roleRepository.save(role);
        }
    }

    @Override
    @Transactional
    @RequireOrganizationAccess(organizationIdParam = "organizationId")
    public void addPermissionToRoleInOrganization(UUID roleId, UUID permissionId, UUID organizationId) {
        if (roleId == null) {
            throw new IllegalArgumentException("Role ID cannot be null");
        }
        if (permissionId == null) {
            throw new IllegalArgumentException("Permission ID cannot be null");
        }
        if (organizationId == null) {
            throw new IllegalArgumentException("Organization ID cannot be null");
        }

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + roleId));


        if (!role.getOrganizationId().equals(organizationId)) {
            throw new EntityNotFoundException("Role not found in the specified organization");
        }

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new EntityNotFoundException("Permission not found with id: " + permissionId));

        if (!permission.getOrganizationId().equals(organizationId)) {
            throw new EntityNotFoundException("Permission not found in the specified organization");
        }


        if (role.getPermissions() == null) {
            role.setPermissions(new HashSet<>());
        }

        if (role.getPermissions().add(permission)) {
            roleRepository.save(role);
        }
    }

    @Override
    @Transactional
    public void removePermissionFromRole(UUID roleId, UUID permissionId) {
        if (roleId == null) {
            throw new IllegalArgumentException("Role ID cannot be null");
        }
        if (permissionId == null) {
            throw new IllegalArgumentException("Permission ID cannot be null");
        }

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + roleId));

        if (role.getPermissions() == null || role.getPermissions().isEmpty()) {
            throw new EntityNotFoundException(
                    "Permission not found with id: " + permissionId + " in role with id: " + roleId);
        }

        boolean removed = role.getPermissions().removeIf(p -> p.getId().equals(permissionId));

        if (!removed) {
            throw new EntityNotFoundException(
                    "Permission not found with id: " + permissionId + " in role with id: " + roleId);
        }

        roleRepository.save(role);
    }

    @Override
    @Transactional
    @RequireOrganizationAccess(organizationIdParam = "organizationId")
    public void removePermissionFromRoleInOrganization(UUID roleId, UUID permissionId, UUID organizationId) {
        if (roleId == null) {
            throw new IllegalArgumentException("Role ID cannot be null");
        }
        if (permissionId == null) {
            throw new IllegalArgumentException("Permission ID cannot be null");
        }
        if (organizationId == null) {
            throw new IllegalArgumentException("Organization ID cannot be null");
        }

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + roleId));


        if (!role.getOrganizationId().equals(organizationId)) {
            throw new EntityNotFoundException("Role not found in the specified organization");
        }

        if (role.getPermissions() == null || role.getPermissions().isEmpty()) {
            throw new EntityNotFoundException(
                    "Permission not found with id: " + permissionId + " in role with id: " + roleId);
        }

        boolean removed = role.getPermissions().removeIf(p -> p.getId().equals(permissionId));

        if (!removed) {
            throw new EntityNotFoundException(
                    "Permission not found with id: " + permissionId + " in role with id: " + roleId);
        }

        roleRepository.save(role);
    }
}