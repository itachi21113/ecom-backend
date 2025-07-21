// src/main/java/com/example/ecommerce/ecom_backend/dto/JwtAuthResponse.java
package com.example.ecommerce.ecom_backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Lombok to generate getters, setters, toString, equals, hashCode
@AllArgsConstructor // Lombok to generate a constructor with all fields
@NoArgsConstructor // Lombok to generate a no-argument constructor
public class JwtAuthResponse {
    private String accessToken;
    private String tokenType = "Bearer"; // Standard token type prefix for JWTs
    // You can add more fields here later if the client needs them immediately after login, e.g.:
    // private Long userId;
    // private String email;
    // private Set<String> roles; // List of role names
    public JwtAuthResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}