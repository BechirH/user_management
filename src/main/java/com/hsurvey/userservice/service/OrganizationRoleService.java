package com.hsurvey.userservice.service;

import com.hsurvey.userservice.entities.Permission;
import com.hsurvey.userservice.entities.Role;
import com.hsurvey.userservice.repositories.PermissionRepository;
import com.hsurvey.userservice.repositories.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class OrganizationRoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public OrganizationRoleService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Transactional
    public Role createDefaultUserRole(UUID organizationId) {
        // Check if default role already exists for this organization
        Optional<Role> existingRole = roleRepository.findByNameAndOrganizationId("USER", organizationId);
        if (existingRole.isPresent()) {
            return existingRole.get();
        }

        // Create default permissions for the organization
        Set<Permission> defaultPermissions = createDefaultPermissions(organizationId);

        // Create default USER role
        Role userRole = Role.builder()
                .name("USER")
                .organizationId(organizationId)
                .description("Default user role")
                .permissions(defaultPermissions)
                .build();

        return roleRepository.save(userRole);
    }

    @Transactional
    public void createDefaultRolesForOrganization(UUID organizationId) {
        // Create default permissions
        Set<Permission> allPermissions = createDefaultPermissions(organizationId);

        // Create USER role (limited permissions)
        Set<Permission> userPermissions = allPermissions.stream()
                .filter(p -> p.getName().equals("READ_PROFILE") || p.getName().equals("UPDATE_PROFILE"))
                .collect(HashSet::new, Set::add, Set::addAll);

        Role userRole = Role.builder()
                .name("USER")
                .organizationId(organizationId)
                .description("Default user role")
                .permissions(userPermissions)
                .build();

        // Create ADMIN role (all permissions)
        Role adminRole = Role.builder()
                .name("ADMIN")
                .organizationId(organizationId)
                .description("Organization administrator role")
                .permissions(new HashSet<>(allPermissions))
                .build();

        // Save roles if they don't exist
        if (!roleRepository.existsByNameAndOrganizationId("USER", organizationId)) {
            roleRepository.save(userRole);
        }

        if (!roleRepository.existsByNameAndOrganizationId("ADMIN", organizationId)) {
            roleRepository.save(adminRole);
        }
    }

    private Set<Permission> createDefaultPermissions(UUID organizationId) {
        List<String> defaultPermissionNames = Arrays.asList(
                "READ_PROFILE",
                "UPDATE_PROFILE",
                "CREATE_SURVEY",
                "READ_SURVEY",
                "UPDATE_SURVEY",
                "DELETE_SURVEY",
                "MANAGE_USERS",
                "VIEW_ANALYTICS"
        );

        Set<Permission> permissions = new HashSet<>();

        for (String permissionName : defaultPermissionNames) {
            // Check if permission already exists
            Optional<Permission> existingPermission = permissionRepository.findByNameAndOrganizationId(permissionName, organizationId);

            if (existingPermission.isPresent()) {
                permissions.add(existingPermission.get());
            } else {
                Permission permission = Permission.builder()
                        .name(permissionName)
                        .organizationId(organizationId)
                        .description("Default " + permissionName.toLowerCase().replace("_", " ") + " permission")
                        .build();

                Permission savedPermission = permissionRepository.save(permission);
                permissions.add(savedPermission);
            }
        }

        return permissions;
    }

    public Role getDefaultUserRole(UUID organizationId) {
        return roleRepository.findByNameAndOrganizationId("USER", organizationId)
                .orElseGet(() -> createDefaultUserRole(organizationId));
    }

    public Optional<Role> findRoleByNameAndOrganization(String roleName, UUID organizationId) {
        return roleRepository.findByNameAndOrganizationId(roleName, organizationId);
    }

    public List<Role> getRolesByOrganization(UUID organizationId) {
        return roleRepository.findByOrganizationId(organizationId);
    }

    public List<Permission> getPermissionsByOrganization(UUID organizationId) {
        return permissionRepository.findByOrganizationId(organizationId);
    }
}