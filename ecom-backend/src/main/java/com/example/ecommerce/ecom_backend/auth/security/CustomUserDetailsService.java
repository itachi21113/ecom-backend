// src/main/java/com/example/ecommerce/ecom_backend/security/CustomUserDetailsService.java
package com.example.ecommerce.ecom_backend.auth.security;

import com.example.ecommerce.ecom_backend.user.model.User; // Your User entity
import com.example.ecommerce.ecom_backend.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service // Marks this class as a Spring Service component
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // Using constructor injection for UserRepository
    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Step 1: Find the user in your database by email
        // We use 'email' as the username for login
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Step 2: Convert your User's roles into Spring Security's GrantedAuthority objects
        // Spring Security expects roles to be prefixed with "ROLE_" (e.g., "ROLE_ADMIN", "ROLE_USER")
        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName())) // Assuming Role has a getName() method
                .collect(Collectors.toSet());

        // Step 3: Return Spring Security's User object
        // This UserDetails object contains the necessary info for Spring Security to perform authentication
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),          // The username (email) used for authentication
                user.getPassword(),       // The hashed password stored in your database
                authorities               // The user's authorities/roles
        );
    }
}