// src/main/java/com/example/ecommerce/ecom_backend/security/JwtAuthenticationFilter.java
package com.example.ecommerce.ecom_backend.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter; // Import this

import java.io.IOException;

@Component // Make this a Spring component
public class JwtAuthenticationFilter extends OncePerRequestFilter { // Extend OncePerRequestFilter

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    // Use constructor injection
    @Autowired
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, CustomUserDetailsService customUserDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Get JWT token from HTTP request header
        String token = getTokenFromRequest(request);

        // 2. Validate token and load user details if token is valid
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            // Get username from token
            String username = jwtTokenProvider.getUsernameFromJwt(token);

            // Load user associated with token
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

            // Create Authentication object
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null, // Credentials are not needed once authenticated via token
                    userDetails.getAuthorities() // User's roles/authorities
            );
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Set Spring Security Authentication in SecurityContextHolder
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        // 3. Continue with the filter chain (pass to the next filter or servlet)
        filterChain.doFilter(request, response);
    }

    // Helper method to extract JWT token from request header
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization"); // Get the "Authorization" header

        // Check if Authorization header contains Bearer token
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // Extract the token part after "Bearer "
            return bearerToken.substring(7);
        }
        return null; // No Bearer token found
    }
}