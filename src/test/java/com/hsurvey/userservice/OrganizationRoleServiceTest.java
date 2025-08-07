package com.hsurvey.userservice;

import com.hsurvey.userservice.entities.Permission;
import com.hsurvey.userservice.entities.Role;
import com.hsurvey.userservice.repositories.PermissionRepository;
import com.hsurvey.userservice.repositories.RoleRepository;
import com.hsurvey.userservice.service.OrganizationRoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationRoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private OrganizationRoleService organizationRoleService;

    private UUID organizationId;

    @BeforeEach
    void setUp() {
        organizationId = UUID.randomUUID();
    }

    @Test
    void testCreateDefaultRolesForOrganization() {
        // Mock existing permissions
        Set<Permission> mockPermissions = createMockPermissions();
        when(permissionRepository.findByNameAndOrganizationId(anyString(), eq(organizationId)))
                .thenReturn(Optional.empty());
        when(permissionRepository.save(any(Permission.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Mock role repository methods
        when(roleRepository.existsByNameAndOrganizationId("USER", organizationId)).thenReturn(false);
        when(roleRepository.existsByNameAndOrganizationId("ORGANIZATION MANAGER", organizationId)).thenReturn(false);
        when(roleRepository.existsByNameAndOrganizationId("DEPARTMENT MANAGER", organizationId)).thenReturn(false);
        when(roleRepository.existsByNameAndOrganizationId("TEAM MANAGER", organizationId)).thenReturn(false);

        // Execute the method
        organizationRoleService.createDefaultRolesForOrganization(organizationId);

        // Verify that all 4 roles were saved
        verify(roleRepository, times(1)).save(argThat(role -> 
            role.getName().equals("USER") && 
            role.getPermissions().isEmpty()));
        
        verify(roleRepository, times(1)).save(argThat(role -> 
            role.getName().equals("ORGANIZATION MANAGER") && 
            !role.getPermissions().isEmpty()));
        
        verify(roleRepository, times(1)).save(argThat(role -> 
            role.getName().equals("DEPARTMENT MANAGER") && 
            !role.getPermissions().isEmpty()));
        
        verify(roleRepository, times(1)).save(argThat(role -> 
            role.getName().equals("TEAM MANAGER") && 
            !role.getPermissions().isEmpty()));
    }

    @Test
    void testGetDefaultUserRole() {
        Role mockUserRole = Role.builder()
                .name("USER")
                .organizationId(organizationId)
                .description("Default user role with no permissions")
                .permissions(new HashSet<>())
                .build();

        when(roleRepository.findByNameAndOrganizationId("USER", organizationId))
                .thenReturn(Optional.of(mockUserRole));

        Role result = organizationRoleService.getDefaultUserRole(organizationId);

        assertNotNull(result);
        assertEquals("USER", result.getName());
        assertTrue(result.getPermissions().isEmpty());
    }

    @Test
    void testGetDefaultOrganizationManagerRole() {
        Role mockOrgManagerRole = Role.builder()
                .name("ORGANIZATION MANAGER")
                .organizationId(organizationId)
                .description("Organization manager role")
                .permissions(new HashSet<>())
                .build();

        when(roleRepository.findByNameAndOrganizationId("ORGANIZATION MANAGER", organizationId))
                .thenReturn(Optional.of(mockOrgManagerRole));

        Role result = organizationRoleService.getDefaultOrganizationManagerRole(organizationId);

        assertNotNull(result);
        assertEquals("ORGANIZATION MANAGER", result.getName());
    }

    @Test
    void testGetDefaultDepartmentManagerRole() {
        Role mockDeptManagerRole = Role.builder()
                .name("DEPARTMENT MANAGER")
                .organizationId(organizationId)
                .description("Department manager role")
                .permissions(new HashSet<>())
                .build();

        when(roleRepository.findByNameAndOrganizationId("DEPARTMENT MANAGER", organizationId))
                .thenReturn(Optional.of(mockDeptManagerRole));

        Role result = organizationRoleService.getDefaultDepartmentManagerRole(organizationId);

        assertNotNull(result);
        assertEquals("DEPARTMENT MANAGER", result.getName());
    }

    @Test
    void testGetDefaultTeamManagerRole() {
        Role mockTeamManagerRole = Role.builder()
                .name("TEAM MANAGER")
                .organizationId(organizationId)
                .description("Team manager role")
                .permissions(new HashSet<>())
                .build();

        when(roleRepository.findByNameAndOrganizationId("TEAM MANAGER", organizationId))
                .thenReturn(Optional.of(mockTeamManagerRole));

        Role result = organizationRoleService.getDefaultTeamManagerRole(organizationId);

        assertNotNull(result);
        assertEquals("TEAM MANAGER", result.getName());
    }

    @Test
    void testBackwardCompatibilityGetDefaultAdminRole() {
        Role mockOrgManagerRole = Role.builder()
                .name("ORGANIZATION MANAGER")
                .organizationId(organizationId)
                .description("Organization manager role")
                .permissions(new HashSet<>())
                .build();

        when(roleRepository.findByNameAndOrganizationId("ORGANIZATION MANAGER", organizationId))
                .thenReturn(Optional.of(mockOrgManagerRole));

        Role result = organizationRoleService.getDefaultAdminRole(organizationId);

        assertNotNull(result);
        assertEquals("ORGANIZATION MANAGER", result.getName());
    }

    private Set<Permission> createMockPermissions() {
        Set<Permission> permissions = new HashSet<>();
        List<String> permissionNames = Arrays.asList(
                "ORG_MANAGER", "DEPARTMENT_MANAGER", "TEAM_MANAGER",
                "SURVEY_READ", "SURVEY_CREATE", "SURVEY_UPDATE", "SURVEY_DELETE",
                "OPTION_READ", "OPTION_CREATE", "OPTION_UPDATE", "OPTION_DELETE",
                "QUESTION_READ", "QUESTION_CREATE", "QUESTION_UPDATE", "QUESTION_DELETE",
                "TEAM_READ", "TEAM_CREATE", "TEAM_UPDATE", "TEAM_DELETE",
                "DEPARTMENT_READ", "DEPARTMENT_CREATE", "DEPARTMENT_UPDATE", "DEPARTMENT_DELETE"
        );

        for (String name : permissionNames) {
            Permission permission = Permission.builder()
                    .name(name)
                    .organizationId(organizationId)
                    .description("Mock permission: " + name)
                    .build();
            permissions.add(permission);
        }

        return permissions;
    }
} 