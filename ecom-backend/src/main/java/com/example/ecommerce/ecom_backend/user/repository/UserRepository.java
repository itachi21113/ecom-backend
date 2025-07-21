package com.example.ecommerce.ecom_backend.user.repository;

import com.example.ecommerce.ecom_backend.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository // Marks this interface as a Spring Data JPA repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail( String email);

    Optional<User> findByUsername(String username);

}
