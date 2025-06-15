package com.hsurvey.userservice.service;

import com.hsurvey.userservice.dto.CreateUserDTO;
import com.hsurvey.userservice.dto.UserDTO;
import java.util.List;
import java.util.UUID;

public interface UserService {
    // Create methods
    UserDTO createUser(CreateUserDTO createUserDTO);
    UserDTO createUserForOrganization(CreateUserDTO createUserDTO, UUID targetOrganizationId);

    // Read methods
    List<UserDTO> getAllUsers();
    List<UserDTO> getAllUsersByOrganization(UUID organizationId);
    UserDTO getUserById(UUID userId);
    UserDTO getUserByIdAndOrganization(UUID userId, UUID organizationId);

    // Check existence method
    boolean existsById(UUID userId);
    // Update methods
    UserDTO updateUser(UUID id, UserDTO userDTO);
    UserDTO updateUserInOrganization(UUID id, UserDTO userDTO, UUID organizationId);

    // Delete methods
    void deleteUserById(UUID userId);
    void deleteUserByIdAndOrganization(UUID userId, UUID organizationId);

    // Role management methods
    UserDTO addRoleToUser(UUID userId, UUID roleId);
    UserDTO addRoleToUserInOrganization(UUID userId, UUID roleId, UUID organizationId);
    UserDTO removeRoleFromUser(UUID userId, UUID roleId);
    UserDTO removeRoleFromUserInOrganization(UUID userId, UUID roleId, UUID organizationId);
}