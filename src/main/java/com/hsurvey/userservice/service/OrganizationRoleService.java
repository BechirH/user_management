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
    public void createDefaultRolesForOrganization(UUID organizationId) {

        Set<Permission> allPermissions = createDefaultPermissions(organizationId);


        Role userRole = Role.builder()
                .name("USER")
                .organizationId(organizationId)
                .description("Default user role with no permissions")
                .permissions(new HashSet<>()) // Empty set - no permissions
                .build();


        Role adminRole = Role.builder()
                .name("ADMIN")
                .organizationId(organizationId)
                .description("Organization administrator role")
                .permissions(new HashSet<>(allPermissions))
                .build();

        if (!roleRepository.existsByNameAndOrganizationId("USER", organizationId)) {
            roleRepository.save(userRole);
        }
        if (!roleRepository.existsByNameAndOrganizationId("ADMIN", organizationId)) {
            roleRepository.save(adminRole);
        }
    }

    private Set<Permission> createDefaultPermissions(UUID organizationId) {
        List<String> defaultPermissionNames = Arrays.asList(
                "PERMISSION_CREATE",
                "PERMISSION_READ",
                "PERMISSION_UPDATE",
                "PERMISSION_DELETE",
                "ROLE_CREATE",
                "ROLE_READ",
                "ROLE_UPDATE",
                "ROLE_DELETE",
                "USER_CREATE",
                "USER_READ",
                "USER_UPDATE",
                "USER_DELETE"

        );

        Set<Permission> permissions = new HashSet<>();

        for (String permissionName : defaultPermissionNames) {
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
                .orElseThrow(() -> new RuntimeException("Default USER role not found for organization. Please contact administrator."));
    }

    public Role getDefaultAdminRole(UUID organizationId) {
        return roleRepository.findByNameAndOrganizationId("ADMIN", organizationId)
                .orElseThrow(() -> new RuntimeException("Default ADMIN role not found for organization."));
    }


}