// src/main/java/com/example/ecommerce/ecom_backend/dto/LoginRequest.java
package com.example.ecommerce.ecom_backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data // Lombok for getters/setters
public class LoginRequest {

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    private String password;
}