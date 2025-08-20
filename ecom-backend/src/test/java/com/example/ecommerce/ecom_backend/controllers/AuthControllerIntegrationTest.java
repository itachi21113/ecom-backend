package com.example.ecommerce.ecom_backend.controllers;

import com.example.ecommerce.ecom_backend.auth.dto.UserRegistrationRequest;
import com.example.ecommerce.ecom_backend.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerIntegrationTest {

    @Container
    static MySQLContainer<?>  mySQLContainer = new MySQLContainer<>("mysql:8.0.26");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // Spring Boot's JSON converter

    @Autowired
    private UserRepository userRepository;

    @DynamicPropertySource
    static void setDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mySQLContainer::getUsername);
        registry.add("spring.datasource.password", mySQLContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");


    }

    @Test
    void registerUser_shouldCreateUser_whenRegistrationRequestIsValid() throws Exception {
        // Arrange
        UserRegistrationRequest registrationRequest = new UserRegistrationRequest();
        registrationRequest.setUsername("integrationTestUser");
        registrationRequest.setEmail("integration@test.com");
        registrationRequest.setPassword("password123");
        registrationRequest.setFirstName("John");
        registrationRequest.setLastName("Doe");

        // Act
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated()) // Assert HTTP Status
                .andExpect(jsonPath("$.email").value("integration@test.com"))
                .andExpect(jsonPath("$.firstName").value("John"));


        // Assert Database State
        var userOptional = userRepository.findByEmail("integration@test.com");
        assertTrue(userOptional.isPresent(), "User should be present in the database");
        assertEquals("integrationTestUser", userOptional.get().getUsername());
        assertEquals("John", userOptional.get().getFirstName());


    }
}
