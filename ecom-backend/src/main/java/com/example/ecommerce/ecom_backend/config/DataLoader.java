package com.example.ecommerce.ecom_backend.config;

import com.example.ecommerce.ecom_backend.order.model.Order;
import com.example.ecommerce.ecom_backend.order.repository.OrderRepository;
import com.example.ecommerce.ecom_backend.user.model.Role;
import com.example.ecommerce.ecom_backend.user.model.User;
import com.example.ecommerce.ecom_backend.user.repository.RoleRepository;
import com.example.ecommerce.ecom_backend.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // We call our single, reliable method to get the roles.
        Role userRole = findOrCreateRole("ROLE_USER");
        findOrCreateRole("ROLE_ADMIN");

        // We pass the guaranteed role to the user/order creation method.
        createTestUserAndOrder(userRole);
    }


    private Role findOrCreateRole(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(roleName);
                    return roleRepository.save(newRole);
                });
    }


    private void createTestUserAndOrder(Role userRole) {
        // We only create the user if one with this email doesn't already exist.
        if (userRepository.findByEmail("testuser@example.com").isEmpty()) {

            User testUser = new User();
            testUser.setFirstName("Test");
            testUser.setLastName("User");
            testUser.setEmail("testuser@example.com");
            testUser.setUsername("testuser@example.com");
            testUser.setPassword(passwordEncoder.encode("password"));
            testUser.setRoles(Collections.singleton(userRole));
            userRepository.save(testUser);

            // Create a test order for this new user.
            Order testOrder = new Order();
            testOrder.setUser(testUser);
            testOrder.setOrderDate(LocalDateTime.now());
            testOrder.setTotalAmount(new BigDecimal("129.99"));
            testOrder.setStatus("PENDING"); // Important: Initial status
            orderRepository.save(testOrder);
            System.out.println("---- Created Test User and Test Order with ID: " + testOrder.getId() + " ----");
        }
    }
}