package com.hsurvey.userservice.mapper;

import com.hsurvey.userservice.dto.UserDTO;
import com.hsurvey.userservice.entities.User;
import com.hsurvey.userservice.entities.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {RoleMapper.class})
public interface UserMapper {

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToStrings")
    @Mapping(target = "organizationId", source = "organizationId")
    UserDTO toDto(User user);

    @Mapping(target = "roles", source = "roles", qualifiedByName = "stringsToRoles")
    @Mapping(target = "organizationId", source = "organizationId", qualifiedByName = "validateOrgId")
    User toEntity(UserDTO userDTO);


    @Named("rolesToStrings")
    default Set<String> rolesToStrings(Set<Role> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

    @Named("stringsToRoles")
    default Set<Role> stringsToRoles(Set<String> roleNames) {
        if (roleNames == null) {
            return Set.of();
        }
        return roleNames.stream()
                .map(name -> {
                    Role role = new Role();
                    role.setName(name);
                    return role;
                })
                .collect(Collectors.toSet());
    }


    @Named("validateOrgId")
    default UUID validateOrganizationId(UUID orgId) {
        if (orgId == null) {
            throw new IllegalArgumentException("Organization ID cannot be null");
        }
        return orgId;
    }
}