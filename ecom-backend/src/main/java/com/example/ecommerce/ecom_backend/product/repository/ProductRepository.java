package com.example.ecommerce.ecom_backend.product.repository;

import com.example.ecommerce.ecom_backend.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Custom query methods for Product can be added here
}