// src/main/java/com/example/ecommerce/ecom_backend/config/DataLoader.java
package com.example.ecommerce.ecom_backend.config;

import com.example.ecommerce.ecom_backend.user.model.Role;
import com.example.ecommerce.ecom_backend.user.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class DataLoader implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public DataLoader(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Check if ROLE_USER exists, if not, create it
        if (roleRepository.findByName("ROLE_USER").isEmpty()) {
            Role userRole = new Role();
            userRole.setName("ROLE_USER");
            roleRepository.save(userRole);
            System.out.println("ROLE_USER created.");
        }

        // Check if ROLE_ADMIN exists, if not, create it
        if (roleRepository.findByName("ROLE_ADMIN").isEmpty()) {
            Role adminRole = new Role();
            adminRole.setName("ROLE_ADMIN");
            roleRepository.save(adminRole);
            System.out.println("ROLE_ADMIN created.");
        }
    }
}