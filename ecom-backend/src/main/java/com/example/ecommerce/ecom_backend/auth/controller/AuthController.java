// src/main/java/com/example/ecommerce/ecom_backend/controller/AuthController.java
package com.example.ecommerce.ecom_backend.auth.controller;

import com.example.ecommerce.ecom_backend.auth.dto.JwtAuthResponse;
import com.example.ecommerce.ecom_backend.auth.dto.LoginRequest;
import com.example.ecommerce.ecom_backend.auth.dto.UserRegistrationRequest; // Import your DTO
import com.example.ecommerce.ecom_backend.user.dto.UserResponseDTO;
import com.example.ecommerce.ecom_backend.auth.security.JwtTokenProvider;
import com.example.ecommerce.ecom_backend.user.service.UserService;
import jakarta.validation.Valid; // Import for @Valid
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus; // Import for HttpStatus
import org.springframework.http.ResponseEntity; // Import for ResponseEntity
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*; // Import all general annotations

@RestController // Marks this class as a REST Controller
@RequestMapping("/api/auth") // Base path for authentication-related endpoints
public class AuthController {

    private final UserService userService; // Use final for constructor injection
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    // Use constructor injection
    @Autowired
    public AuthController(UserService userService, AuthenticationManager authenticationManager ,JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;

    }

    @PostMapping("/register") // Handles POST requests to /api/auth/register
    public ResponseEntity<UserResponseDTO> registerUser(@Valid @RequestBody UserRegistrationRequest registrationRequest) {
        UserResponseDTO registeredUser = userService.registerUser(registrationRequest);

        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }
    @PostMapping("/login") // Handles POST requests to /api/auth/login
    public ResponseEntity<JwtAuthResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // Step 1: Authenticate the user using AuthenticationManager
        // This process uses our CustomUserDetailsService and PasswordEncoder
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        // Step 2: Set the authenticated user in Spring Security's context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Step 3: GENERATE THE REAL JWT TOKEN HERE
        String token = jwtTokenProvider.generateToken(authentication); // Use JwtTokenProvider to generate the token

        // Return the JWT in the response
        JwtAuthResponse jwtAuthResponse = new JwtAuthResponse();
        jwtAuthResponse.setAccessToken(token); // Set the actual generated token

        return new ResponseEntity<>(jwtAuthResponse, HttpStatus.OK);
    }

}