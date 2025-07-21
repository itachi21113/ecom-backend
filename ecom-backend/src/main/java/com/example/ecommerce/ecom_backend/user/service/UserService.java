// src/main/java/com/example/ecommerce/ecom_backend/service/UserService.java
package com.example.ecommerce.ecom_backend.user.service;

import com.example.ecommerce.ecom_backend.auth.dto.UserRegistrationRequest;
import com.example.ecommerce.ecom_backend.user.dto.UserResponseDTO; // Import the new DTO
import com.example.ecommerce.ecom_backend.user.dto.UserUpdateDTO;   // Import the new DTO
import com.example.ecommerce.ecom_backend.common.exception.DuplicateEmailException; // Import new exception
import com.example.ecommerce.ecom_backend.common.exception.ResourceNotFoundException; // Assuming this exists or create it if not

import java.util.List;

public interface UserService {
    List<UserResponseDTO> getAllUsers(); // Returns DTOs
    UserResponseDTO getUserById(Long id) throws ResourceNotFoundException; // Returns DTO, throws exception
    UserResponseDTO updateUser(Long id, UserUpdateDTO userUpdateDTO) throws ResourceNotFoundException; // Takes DTO, returns DTO, throws exception
    void deleteUser(Long id) throws ResourceNotFoundException; // Throws exception
    UserResponseDTO registerUser(UserRegistrationRequest registrationRequest) throws DuplicateEmailException; // Returns DTO, throws exception
}