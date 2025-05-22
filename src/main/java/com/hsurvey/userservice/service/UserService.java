package com.hsurvey.userservice.service;

import com.hsurvey.userservice.dto.UserDTO;
import java.util.List;

public interface UserService {
    UserDTO createUser(UserDTO userDTO);
    List<UserDTO> getAllUsers();
    UserDTO getUserById(Long id);
    UserDTO updateUser(Long id, UserDTO userDTO);
    void deleteUser(Long id);
    UserDTO addRoleToUser(Long userId, Long roleId);
    UserDTO removeRoleFromUser(Long userId, Long roleId);
}