package com.hsurvey.userservice.mapper;

import com.hsurvey.userservice.dto.RoleDTO;
import com.hsurvey.userservice.entities.Role;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {PermissionMapper.class})
public interface RoleMapper {

    RoleDTO toDto(Role role);

    Role toEntity(RoleDTO roleDTO);
}
