package com.hsurvey.userservice.service.impl;

import com.hsurvey.userservice.dto.CreateUserDTO;
import com.hsurvey.userservice.dto.UserDTO;
import com.hsurvey.userservice.entities.User;
import com.hsurvey.userservice.entities.Role;
import com.hsurvey.userservice.mapper.UserMapper;
import com.hsurvey.userservice.repositories.UserRepository;
import com.hsurvey.userservice.repositories.RoleRepository;
import com.hsurvey.userservice.service.UserService;
import com.hsurvey.userservice.annotation.RequireOrganizationAccess;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           UserMapper userMapper,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    // CREATE METHODS
    @Override
    @Transactional
    public UserDTO createUser(CreateUserDTO createUserDTO) {
        return createUserInternal(createUserDTO, createUserDTO.getOrganizationId());
    }

    @Override
    @Transactional
    public UserDTO createUserForOrganization(CreateUserDTO createUserDTO, UUID targetOrganizationId) {
        return createUserInternal(createUserDTO, targetOrganizationId);
    }

    private UserDTO createUserInternal(CreateUserDTO createUserDTO, UUID organizationId) {
        if (organizationId == null) {
            throw new IllegalArgumentException("Organization ID is required");
        }
        if (userRepository.existsByUsernameAndOrganizationId(createUserDTO.getUsername(), organizationId)) {
            throw new IllegalArgumentException("Username already exists in this organization");
        }
        if (userRepository.existsByEmailAndOrganizationId(createUserDTO.getEmail(), organizationId)) {
            throw new IllegalArgumentException("Email already exists in this organization");
        }

        User user = new User();
        user.setUsername(createUserDTO.getUsername());
        user.setEmail(createUserDTO.getEmail());
        user.setPassword(passwordEncoder.encode(createUserDTO.getPassword()));
        user.setOrganizationId(organizationId);
        user.setDepartmentId(createUserDTO.getDepartmentId());
        user.setTeamId(createUserDTO.getTeamId());
        user.setRoles(new HashSet<>());

        user = userRepository.save(user);
        return userMapper.toDto(user);
    }

    // READ METHODS
    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsersByOrganization(UUID organizationId) {
        if (organizationId == null) {
            throw new IllegalArgumentException("Organization ID cannot be null");
        }

        return userRepository.findByOrganizationId(organizationId).stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    @RequireOrganizationAccess(organizationIdParam = "organizationId")
    public UserDTO getUserByIdAndOrganization(UUID userId, UUID organizationId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (organizationId == null) {
            throw new IllegalArgumentException("Organization ID cannot be null");
        }

        User user = userRepository.findByIdAndOrganizationId(userId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId + " in organization: " + organizationId));

        return userMapper.toDto(user);
    }
    // CHECK EXISTANCE
    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID userId) {
        if (userId == null) {
            return false;
        }
        return userRepository.existsById(userId);
    }

    // UPDATE METHODS
    @Override
    @Transactional
    public UserDTO updateUser(UUID id, UserDTO userDTO) {
        if (id == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        return updateUserInternal(existingUser, userDTO);
    }

    @Override
    @Transactional
    @RequireOrganizationAccess(organizationIdParam = "organizationId")
    public UserDTO updateUserInOrganization(UUID id, UserDTO userDTO, UUID organizationId) {
        if (id == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (organizationId == null) {
            throw new IllegalArgumentException("Organization ID cannot be null");
        }

        User existingUser = userRepository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id + " in organization: " + organizationId));


        if (!existingUser.getUsername().equals(userDTO.getUsername()) &&
                userRepository.existsByUsernameAndOrganizationId(userDTO.getUsername(), organizationId)) {
            throw new IllegalArgumentException("Username already exists in this organization");
        }


        if (!existingUser.getEmail().equals(userDTO.getEmail()) &&
                userRepository.existsByEmailAndOrganizationId(userDTO.getEmail(), organizationId)) {
            throw new IllegalArgumentException("Email already exists in this organization");
        }

        return updateUserInternal(existingUser, userDTO);
    }

    private UserDTO updateUserInternal(User existingUser, UserDTO userDTO) {
        existingUser.setUsername(userDTO.getUsername());
        existingUser.setEmail(userDTO.getEmail());
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        existingUser.setDepartmentId(userDTO.getDepartmentId());
        existingUser.setTeamId(userDTO.getTeamId());

        User updatedUser = userRepository.save(existingUser);
        return userMapper.toDto(updatedUser);
    }

    // DELETE METHODS
    @Override
    @Transactional
    public void deleteUserById(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        userRepository.delete(user);
    }

    @Override
    @Transactional
    @RequireOrganizationAccess(organizationIdParam = "organizationId")
    public void deleteUserByIdAndOrganization(UUID userId, UUID organizationId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (organizationId == null) {
            throw new IllegalArgumentException("Organization ID cannot be null");
        }

        User user = userRepository.findByIdAndOrganizationId(userId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId + " in organization: " + organizationId));

        userRepository.delete(user);
    }

    // ROLE MANAGEMENT METHODS
    @Override
    @Transactional
    public UserDTO addRoleToUser(UUID userId, UUID roleId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (roleId == null) {
            throw new IllegalArgumentException("Role ID cannot be null");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + roleId));

        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }

        if (user.getRoles().add(role)) {
            User updatedUser = userRepository.save(user);
            return userMapper.toDto(updatedUser);
        }

        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    @RequireOrganizationAccess(organizationIdParam = "organizationId")
    public UserDTO addRoleToUserInOrganization(UUID userId, UUID roleId, UUID organizationId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (roleId == null) {
            throw new IllegalArgumentException("Role ID cannot be null");
        }
        if (organizationId == null) {
            throw new IllegalArgumentException("Organization ID cannot be null");
        }

        User user = userRepository.findByIdAndOrganizationId(userId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId + " in organization: " + organizationId));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + roleId));

        if (!role.getOrganizationId().equals(organizationId)) {
            throw new EntityNotFoundException("Role not found in the specified organization");
        }

        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }

        if (user.getRoles().add(role)) {
            User updatedUser = userRepository.save(user);
            return userMapper.toDto(updatedUser);
        }

        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public UserDTO removeRoleFromUser(UUID userId, UUID roleId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (roleId == null) {
            throw new IllegalArgumentException("Role ID cannot be null");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            throw new EntityNotFoundException("Role not found with id: " + roleId + " for user with id: " + userId);
        }

        boolean removed = user.getRoles().removeIf(r -> r.getId().equals(roleId));

        if (!removed) {
            throw new EntityNotFoundException("Role not found with id: " + roleId + " for user with id: " + userId);
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toDto(updatedUser);
    }

    @Override
    @Transactional
    @RequireOrganizationAccess(organizationIdParam = "organizationId")
    public UserDTO removeRoleFromUserInOrganization(UUID userId, UUID roleId, UUID organizationId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (roleId == null) {
            throw new IllegalArgumentException("Role ID cannot be null");
        }
        if (organizationId == null) {
            throw new IllegalArgumentException("Organization ID cannot be null");
        }

        User user = userRepository.findByIdAndOrganizationId(userId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId + " in organization: " + organizationId));

        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            throw new EntityNotFoundException("Role not found with id: " + roleId + " for user with id: " + userId);
        }

        boolean removed = user.getRoles().removeIf(r -> r.getId().equals(roleId));

        if (!removed) {
            throw new EntityNotFoundException("Role not found with id: " + roleId + " for user with id: " + userId);
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toDto(updatedUser);
    }
}