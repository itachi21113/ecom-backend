// src/main/java/com/example/ecommerce/ecom_backend/service/UserServiceImpl.java
package com.example.ecommerce.ecom_backend.user.service;

import com.example.ecommerce.ecom_backend.auth.dto.UserRegistrationRequest;
import com.example.ecommerce.ecom_backend.user.dto.UserResponseDTO; // New import
import com.example.ecommerce.ecom_backend.user.dto.UserUpdateDTO;   // New import
import com.example.ecommerce.ecom_backend.common.exception.DuplicateEmailException; // New import
import com.example.ecommerce.ecom_backend.common.exception.ResourceNotFoundException; // Assuming this exists
import com.example.ecommerce.ecom_backend.user.model.Role;
import com.example.ecommerce.ecom_backend.user.model.User;
import com.example.ecommerce.ecom_backend.user.repository.RoleRepository;
import com.example.ecommerce.ecom_backend.user.repository.UserRepository; // Adjusted import for UserRepository based on User.java's package

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import for @Transactional

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors; // New import for stream operations

@Service // Marks this class as a Spring Service component
@Transactional // Ensures transactional behavior for service methods
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    // Helper method to convert User entity to UserResponseDTO
    private UserResponseDTO convertToDto(User user) {
        if (user == null) {
            return null;
        }
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        // Map roles to a set of role names (strings)
        if (user.getRoles() != null) {
            dto.setRoles(user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet()));
        } else {
            dto.setRoles(new HashSet<>());
        }
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }

    @Override
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDTO getUserById(Long id) throws ResourceNotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id : '" + id + "'"));
        return convertToDto(user);
    }

    @Override
    @Transactional // Ensure this method is transactional for role updates
    public UserResponseDTO updateUser(Long id, UserUpdateDTO userUpdateDTO) throws ResourceNotFoundException {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id : '" + id + "'"));

        // Update fields from DTO
        // Check if username is provided and update if different
        if (userUpdateDTO.getUsername() != null && !userUpdateDTO.getUsername().equals(existingUser.getUsername())) {
            existingUser.setUsername(userUpdateDTO.getUsername());
        }
        // Check if email is provided, different, AND not already taken by another user
        if (userUpdateDTO.getEmail() != null && !userUpdateDTO.getEmail().equals(existingUser.getEmail())) {
            if (userRepository.findByEmail(userUpdateDTO.getEmail()).isPresent() &&
                    !userRepository.findByEmail(userUpdateDTO.getEmail()).get().getId().equals(id)) {
                throw new DuplicateEmailException("User with email '" + userUpdateDTO.getEmail() + "' already exists.");
            }
            existingUser.setEmail(userUpdateDTO.getEmail());
        }

        if (userUpdateDTO.getFirstName() != null) {
            existingUser.setFirstName(userUpdateDTO.getFirstName());
        }
        if (userUpdateDTO.getLastName() != null) {
            existingUser.setLastName(userUpdateDTO.getLastName());
        }

        // Handle password update only if provided in DTO
        if (userUpdateDTO.getPassword() != null && !userUpdateDTO.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userUpdateDTO.getPassword()));
        }

        // Handle roles update if provided in DTO (ADMIN-specific functionality)
        if (userUpdateDTO.getRoles() != null && !userUpdateDTO.getRoles().isEmpty()) {
            Set<Role> newRoles = new HashSet<>();
            for (String roleName : userUpdateDTO.getRoles()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found: '" + roleName + "'"));
                newRoles.add(role);
            }
            existingUser.setRoles(newRoles);
        } else if (userUpdateDTO.getRoles() != null && userUpdateDTO.getRoles().isEmpty()) {
            // If an empty set is explicitly sent, clear existing roles (e.g., if admin wants to remove all roles)
            existingUser.setRoles(new HashSet<>());
        }

        User updatedUser = userRepository.save(existingUser);
        return convertToDto(updatedUser);
    }


    @Override
    public void deleteUser(Long id) throws ResourceNotFoundException {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id : '" + id + "'");
        }
        userRepository.deleteById(id);
    }

    @Override
    public UserResponseDTO registerUser(UserRegistrationRequest registrationRequest) throws DuplicateEmailException {
        if (userRepository.findByEmail(registrationRequest.getEmail()).isPresent()) {
            throw new DuplicateEmailException("User with email '" + registrationRequest.getEmail() + "' already exists.");
        }

        User newUser = new User();
        newUser.setEmail(registrationRequest.getEmail());
        newUser.setFirstName(registrationRequest.getFirstName());
        newUser.setLastName(registrationRequest.getLastName());
        // Set username if provided, otherwise you might set it to email or generate one
        newUser.setUsername(registrationRequest.getUsername() != null && !registrationRequest.getUsername().trim().isEmpty()
                ? registrationRequest.getUsername()
                : registrationRequest.getEmail()); // Default to email if username is not provided

        String encodedPassword = passwordEncoder.encode(registrationRequest.getPassword());
        newUser.setPassword(encodedPassword);

        Set<Role> roles = new HashSet<>();
        // Assign default role "ROLE_USER"
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName("ROLE_USER");
                    return roleRepository.save(newRole);
                });
        roles.add(userRole);
        newUser.setRoles(roles);

        User savedUser = userRepository.save(newUser);
        return convertToDto(savedUser);
    }
}