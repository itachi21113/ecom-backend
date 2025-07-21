// src/main/java/com/example/ecommerce/ecom_backend/dto/UserResponseDTO.java
package com.example.ecommerce.ecom_backend.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set; // For roles

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Set<String> roles; // Send roles as a set of strings (e.g., "ROLE_USER", "ROLE_ADMIN")
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}