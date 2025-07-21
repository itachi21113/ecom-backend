// src/main/java/com/example/ecommerce/ecom_backend/service/ProductService.java
package com.example.ecommerce.ecom_backend.product.service;

import com.example.ecommerce.ecom_backend.product.dto.ProductRequestDTO; // Import DTOs
import com.example.ecommerce.ecom_backend.product.dto.ProductResponseDTO;
// No need to import Product (entity) or Optional here, as DTOs will be used

import java.util.List;

public interface ProductService {

    // Return DTOs, take DTOs for input
    ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO);

    List<ProductResponseDTO> getAllProducts();

    ProductResponseDTO getProductById(Long id); // Throws exception if not found

    ProductResponseDTO updateProduct(Long id, ProductRequestDTO productRequestDTO); // Throws exception if not found

    void deleteProduct(Long id); // Throws exception if not found
}