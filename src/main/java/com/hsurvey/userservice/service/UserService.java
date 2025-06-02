package com.hsurvey.userservice.service;

import com.hsurvey.userservice.dto.CreateUserDTO;
import com.hsurvey.userservice.dto.UserDTO;
import java.util.List;
import java.util.UUID;

public interface UserService {
    UserDTO createUser(CreateUserDTO createUserDTO);
    List<UserDTO> getAllUsers();
    UserDTO getUserById(UUID id);
    UserDTO updateUser(UUID id, UserDTO userDTO);
    void deleteUser(UUID id);
    UserDTO addRoleToUser(UUID userId, UUID roleId);
    UserDTO removeRoleFromUser(UUID userId, UUID roleId);
}