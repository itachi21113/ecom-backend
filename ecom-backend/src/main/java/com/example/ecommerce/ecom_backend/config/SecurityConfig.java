package com.example.ecommerce.ecom_backend.config;

import com.example.ecommerce.ecom_backend.auth.security.CustomUserDetailsService;
import com.example.ecommerce.ecom_backend.auth.security.JwtAuthEntryPoint;
import com.example.ecommerce.ecom_backend.auth.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
// We'll also need imports for CorsConfigurationSource and UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity // Enables Spring Security's web security features
@EnableMethodSecurity
public class SecurityConfig {


    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter; // Inject your JWT Filter
    private final JwtAuthEntryPoint authenticationEntryPoint; // Inject the Authentication Entry Point

    // Inject your CustomUserDetailsService
    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          JwtAuthenticationFilter jwtAuthenticationFilter,
                          JwtAuthEntryPoint authenticationEntryPoint) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    // Bean for PasswordEncoder (you should already have this)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Bean to expose the AuthenticationManager
    // This is crucial for your AuthController to perform authentication
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // Configures the DaoAuthenticationProvider to use our UserDetailsService and PasswordEncoder
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService); // Set our custom UserDetailsService
        authProvider.setPasswordEncoder(passwordEncoder());    // Set our PasswordEncoder
        return authProvider;
    }


    // Configures the security filter chain (HTTP security rules)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for stateless REST APIs using JWT
                .authorizeHttpRequests(authorize -> authorize
                        // Allow public access to registration and login endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        // All other requests require authentication (e.g., /api/users/**, /api/products/**)
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint) // Set custom entry point for unauthorized access
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Use stateless sessions for JWT
                );
        // Add the JWT filter BEFORE the UsernamePasswordAuthenticationFilter
        // This ensures our JWT filter runs first to authenticate via token
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}