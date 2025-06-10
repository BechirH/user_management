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
import com.hsurvey.userservice.exception.OrganizationAccessException;
import com.hsurvey.userservice.annotation.RequireOrganizationAccess;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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

    @Override
    @Transactional
    public RoleDTO createRole(RoleDTO roleDTO) {

        if (roleDTO.getOrganizationId() == null) {
            throw new IllegalArgumentException("Organization ID is required");
        }

        if (roleRepository.existsByNameAndOrganizationId(roleDTO.getName(), roleDTO.getOrganizationId())) {
            throw new IllegalArgumentException("Role with name '" + roleDTO.getName() +
                    "' already exists in this organization");
        }
        Role role = roleMapper.toEntity(roleDTO);
        if (role.getPermissions() == null) {
            role.setPermissions(new HashSet<>());
        }

        Role savedRole = roleRepository.save(role);
        return roleMapper.toDto(savedRole);
    }

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
        return roleRepository.findById(roleId)
                .map(roleMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + roleId));
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDTO getRoleByNameAndOrganization(String roleName, UUID organizationId) {
        if (roleName == null || organizationId == null) {
            throw new IllegalArgumentException("Role name and organization ID cannot be null");
        }

        return roleRepository.findByNameAndOrganizationId(roleName, organizationId)
                .map(roleMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Role not found with name: " + roleName + " in organization: " + organizationId));
    }

    @Override
    @Transactional
    public RoleDTO updateRole(UUID roleId, RoleDTO roleDTO) {
        Role existingRole = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + roleId));


        if (roleDTO.getOrganizationId() != null &&
                !existingRole.getOrganizationId().equals(roleDTO.getOrganizationId())) {
            throw new IllegalArgumentException("Cannot change organization ID of existing role");
        }

        if (!existingRole.getName().equals(roleDTO.getName()) &&
                roleRepository.existsByNameAndOrganizationId(roleDTO.getName(), existingRole.getOrganizationId())) {
            throw new IllegalArgumentException("Role with name '" + roleDTO.getName() +
                    "' already exists in this organization");
        }
        existingRole.setName(roleDTO.getName());
        existingRole.setDescription(roleDTO.getDescription());
        Role updatedRole = roleRepository.save(existingRole);
        return roleMapper.toDto(updatedRole);
    }

    @Override
    @Transactional
    public void deleteRole(UUID roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + roleId));
        roleRepository.delete(role);
    }

    @Override
    @Transactional
    public void addPermissionToRole(UUID roleId, UUID permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + roleId));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new EntityNotFoundException("Permission not found with id: " + permissionId));
        if (!role.getOrganizationId().equals(permission.getOrganizationId())) {
            throw new IllegalArgumentException("Permission and role must belong to the same organization");
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
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + roleId));

        if (role.getPermissions() == null) {
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
    public void addPermissionsToRole(UUID roleId, List<UUID> permissionIds) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + roleId));
        List<Permission> permissions = permissionRepository.findAllById(permissionIds);

        if (permissions.size() != permissionIds.size()) {
            throw new EntityNotFoundException("Some permissions were not found");
        }

        boolean invalidPermissions = permissions.stream()
                .anyMatch(p -> !p.getOrganizationId().equals(role.getOrganizationId()));

        if (invalidPermissions) {
            throw new IllegalArgumentException("All permissions must belong to the same organization as the role");
        }


        if (role.getPermissions() == null) {
            role.setPermissions(new HashSet<>());
        }

        role.getPermissions().addAll(permissions);
        roleRepository.save(role);
    }

    @Override
    @Transactional
    public void removePermissionsFromRole(UUID roleId, List<UUID> permissionIds) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + roleId));

        if (role.getPermissions() == null || role.getPermissions().isEmpty()) {
            return;
        }

        permissionIds.forEach(permissionId ->
                role.getPermissions().removeIf(p -> p.getId().equals(permissionId))
        );

        roleRepository.save(role);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNameAndOrganization(String roleName, UUID organizationId) {
        if (roleName == null || organizationId == null) {
            return false;
        }
        return roleRepository.existsByNameAndOrganizationId(roleName, organizationId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RoleDTO> findByNameAndOrganization(String roleName, UUID organizationId) {
        if (roleName == null || organizationId == null) {
            return Optional.empty();
        }

        return roleRepository.findByNameAndOrganizationId(roleName, organizationId)
                .map(roleMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @RequireOrganizationAccess(organizationIdParam = "organizationId")
    public RoleDTO getRoleByIdAndOrganization(UUID roleId, UUID organizationId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + roleId));
        return roleMapper.toDto(role);
    }

    @Override
    @Transactional
    @RequireOrganizationAccess(organizationIdParam = "organizationId")
    public void deleteRoleByIdAndOrganization(UUID roleId, UUID organizationId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + roleId));
        roleRepository.delete(role);
    }

    @Override
    @Transactional
    @RequireOrganizationAccess(organizationIdParam = "organizationId")
    public void addPermissionToRoleInOrganization(UUID roleId, UUID permissionId, UUID organizationId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + roleId));
        if (!role.getOrganizationId().equals(organizationId)) {
            throw new OrganizationAccessException("Access denied: Role belongs to different organization");
        }

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new EntityNotFoundException("Permission not found with id: " + permissionId));
        if (!permission.getOrganizationId().equals(organizationId)) {
            throw new OrganizationAccessException("Access denied: Permission belongs to different organization");
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
    @RequireOrganizationAccess(organizationIdParam = "organizationId")
    public void removePermissionFromRoleInOrganization(UUID roleId, UUID permissionId, UUID organizationId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + roleId));
        if (!role.getOrganizationId().equals(organizationId)) {
            throw new OrganizationAccessException("Access denied: Role belongs to different organization");
        }
        if (role.getPermissions() == null) {
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