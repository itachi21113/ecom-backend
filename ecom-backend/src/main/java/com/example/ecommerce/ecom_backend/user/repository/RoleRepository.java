package com.example.ecommerce.ecom_backend.user.repository;

import com.example.ecommerce.ecom_backend.user.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
    // Custom query methods for Role can be added here, e.g.:
    // Optional<Role> findByName(String name);
}