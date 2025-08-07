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

        // Create USER role - empty, no permissions
        Role userRole = Role.builder()
                .name("USER")
                .organizationId(organizationId)
                .description("Default user role with no permissions")
                .permissions(new HashSet<>())
                .build();

        // Create ORGANIZATION_MANAGER role - has all permissions except DEPARTMENT_MANAGER and TEAM_MANAGER
        Set<Permission> organizationManagerPermissions = new HashSet<>(allPermissions);
        organizationManagerPermissions.removeIf(permission -> 
            permission.getName().equals("DEPARTMENT_MANAGER") || 
            permission.getName().equals("TEAM_MANAGER"));

        Role organizationManagerRole = Role.builder()
                .name("ORGANIZATION_MANAGER")
                .organizationId(organizationId)
                .description("Organization manager role with all permissions except department and team management")
                .permissions(organizationManagerPermissions)
                .build();

        // Create DEPARTMENT_MANAGER role - specific permissions
        Set<Permission> departmentManagerPermissions = getDepartmentManagerPermissions(allPermissions);

        Role departmentManagerRole = Role.builder()
                .name("DEPARTMENT_MANAGER")
                .organizationId(organizationId)
                .description("Department manager role with survey, option, question, team, and department permissions")
                .permissions(departmentManagerPermissions)
                .build();

        // Create TEAM_MANAGER role - specific permissions
        Set<Permission> teamManagerPermissions = getTeamManagerPermissions(allPermissions);

        Role teamManagerRole = Role.builder()
                .name("TEAM_MANAGER")
                .organizationId(organizationId)
                .description("Team manager role with survey, option, question, and team permissions")
                .permissions(teamManagerPermissions)
                .build();

        // Save roles if they don't exist
        if (!roleRepository.existsByNameAndOrganizationId("USER", organizationId)) {
            roleRepository.save(userRole);
        }
        if (!roleRepository.existsByNameAndOrganizationId("ORGANIZATION_MANAGER", organizationId)) {
            roleRepository.save(organizationManagerRole);
        }
        if (!roleRepository.existsByNameAndOrganizationId("DEPARTMENT_MANAGER", organizationId)) {
            roleRepository.save(departmentManagerRole);
        }
        if (!roleRepository.existsByNameAndOrganizationId("TEAM_MANAGER", organizationId)) {
            roleRepository.save(teamManagerRole);
        }
    }

    private Set<Permission> getDepartmentManagerPermissions(Set<Permission> allPermissions) {
        Set<Permission> departmentManagerPermissions = new HashSet<>();
        
        // Filter permissions for DEPARTMENT_MANAGER role
        for (Permission permission : allPermissions) {
            String permissionName = permission.getName();
            if (permissionName.equals("DEPARTMENT_MANAGER") ||
                permissionName.equals("SURVEY_READ") ||
                permissionName.equals("SURVEY_CREATE") ||
                permissionName.equals("SURVEY_UPDATE") ||
                permissionName.equals("SURVEY_DELETE") ||
                permissionName.equals("SURVEY_LOCK") ||
                permissionName.equals("SURVEY_UNLOCK") ||
                permissionName.equals("OPTION_CREATE") ||
                permissionName.equals("OPTION_READ") ||
                permissionName.equals("OPTION_UPDATE") ||
                permissionName.equals("OPTION_DELETE") ||
                permissionName.equals("OPTION_LOCK") ||
                permissionName.equals("OPTION_UNLOCK") ||
                permissionName.equals("QUESTION_CREATE") ||
                permissionName.equals("QUESTION_READ") ||
                permissionName.equals("QUESTION_UPDATE") ||
                permissionName.equals("QUESTION_DELETE") ||
                permissionName.equals("QUESTION_LOCK") ||
                permissionName.equals("QUESTION_UNLOCK") ||
                permissionName.equals("TEAM_CREATE") ||
                permissionName.equals("TEAM_READ") ||
                permissionName.equals("TEAM_UPDATE") ||
                permissionName.equals("TEAM_DELETE") ||
                permissionName.equals("DEPARTMENT_READ") ||
                permissionName.equals("DEPARTMENT_UPDATE")) {
                departmentManagerPermissions.add(permission);
            }
        }
        
        return departmentManagerPermissions;
    }

    private Set<Permission> getTeamManagerPermissions(Set<Permission> allPermissions) {
        Set<Permission> teamManagerPermissions = new HashSet<>();
        
        // Filter permissions for TEAM_MANAGER role
        for (Permission permission : allPermissions) {
            String permissionName = permission.getName();
            if (permissionName.equals("TEAM_MANAGER") ||
                permissionName.equals("SURVEY_READ") ||
                permissionName.equals("SURVEY_CREATE") ||
                permissionName.equals("SURVEY_UPDATE") ||
                permissionName.equals("SURVEY_DELETE") ||
                permissionName.equals("SURVEY_LOCK") ||
                permissionName.equals("SURVEY_UNLOCK") ||
                permissionName.equals("OPTION_CREATE") ||
                permissionName.equals("OPTION_READ") ||
                permissionName.equals("OPTION_UPDATE") ||
                permissionName.equals("OPTION_DELETE") ||
                permissionName.equals("OPTION_LOCK") ||
                permissionName.equals("OPTION_UNLOCK") ||
                permissionName.equals("QUESTION_CREATE") ||
                permissionName.equals("QUESTION_READ") ||
                permissionName.equals("QUESTION_UPDATE") ||
                permissionName.equals("QUESTION_DELETE") ||
                permissionName.equals("QUESTION_LOCK") ||
                permissionName.equals("QUESTION_UNLOCK") ||
                permissionName.equals("TEAM_READ") ||
                permissionName.equals("TEAM_UPDATE")) {
                teamManagerPermissions.add(permission);
            }
        }
        
        return teamManagerPermissions;
    }

    private Set<Permission> createDefaultPermissions(UUID organizationId) {
        List<String> defaultPermissionNames = Arrays.asList(
                "ORG_MANAGER",
                "DEPARTMENT_MANAGER",
                "TEAM_MANAGER",
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
                "USER_DELETE",
                "SURVEY_READ",
                "SURVEY_CREATE",
                "SURVEY_UPDATE",
                "SURVEY_DELETE",
                "SURVEY_LOCK",
                "SURVEY_UNLOCK",
                "OPTION_CREATE",
                "OPTION_READ",
                "OPTION_UPDATE",
                "OPTION_DELETE",
                "OPTION_LOCK",
                "OPTION_UNLOCK",
                "QUESTION_CREATE",
                "QUESTION_READ",
                "QUESTION_UPDATE",
                "QUESTION_DELETE",
                "QUESTION_LOCK",
                "QUESTION_UNLOCK",
                "ORGANIZATION_READ",
                "ORGANIZATION_UPDATE",
                "ORGANIZATION_DELETE",
                "DEPARTMENT_CREATE",
                "DEPARTMENT_READ",
                "DEPARTMENT_UPDATE",
                "DEPARTMENT_DELETE",
                "TEAM_CREATE",
                "TEAM_READ",
                "TEAM_UPDATE",
                "TEAM_DELETE"
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

    public Role getDefaultOrganizationManagerRole(UUID organizationId) {
        return roleRepository.findByNameAndOrganizationId("ORGANIZATION_MANAGER", organizationId)
                .orElseThrow(() -> new RuntimeException("Default ORGANIZATION_MANAGER role not found for organization."));
    }

    public Role getDefaultDepartmentManagerRole(UUID organizationId) {
        return roleRepository.findByNameAndOrganizationId("DEPARTMENT_MANAGER", organizationId)
                .orElseThrow(() -> new RuntimeException("Default DEPARTMENT_MANAGER role not found for organization."));
    }

    public Role getDefaultTeamManagerRole(UUID organizationId) {
        return roleRepository.findByNameAndOrganizationId("TEAM_MANAGER", organizationId)
                .orElseThrow(() -> new RuntimeException("Default TEAM_MANAGER role not found for organization."));
    }

    // Keep the old method for backward compatibility, but redirect to ORGANIZATION_MANAGER
    public Role getDefaultAdminRole(UUID organizationId) {
        return getDefaultOrganizationManagerRole(organizationId);
    }
}