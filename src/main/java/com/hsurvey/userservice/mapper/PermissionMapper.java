package com.hsurvey.userservice.mapper;

import com.hsurvey.userservice.dto.PermissionDTO;
import com.hsurvey.userservice.entities.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {

    PermissionDTO toDto(Permission permission);

    Permission toEntity(PermissionDTO permissionDTO);
}
