// src/main/java/com/example/ecommerce/ecom_backend/dto/UserUpdateDTO.java
package com.example.ecommerce.ecom_backend.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDTO {

    // These fields can be blank if not updated, butNotBlank ensures they are not just whitespace if present
    @NotBlank(message = "Username cannot be empty")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "First name cannot be empty")
    private String firstName;

    @NotBlank(message = "Last name cannot be empty")
    private String lastName;

    // Password is optional for update, so no @NotBlank unless you require it for all updates
    // If provided, it should meet certain strength requirements.
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password; // Optional: only if updating password

    // For updating roles - this should be carefully managed, typically by ADMINs only
    private Set<String> roles; // New roles to assign, e.g., ["ROLE_ADMIN", "ROLE_USER"]
}