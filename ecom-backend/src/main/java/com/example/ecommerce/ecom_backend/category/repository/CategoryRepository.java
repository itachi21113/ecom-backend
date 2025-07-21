// src/main/java/com/example/ecommerce/ecom_backend/category/repository/CategoryRepository.java
package com.example.ecommerce.ecom_backend.category.repository;

import com.example.ecommerce.ecom_backend.category.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository // Marks this interface as a Spring Data JPA repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Custom method to find a Category by its unique name
    Optional<Category> findByName(String name);
}