// src/main/java/com/example/ecommerce/ecom_backend/controller/ProductController.java
package com.example.ecommerce.ecom_backend.product.controller;

import com.example.ecommerce.ecom_backend.product.dto.ProductRequestDTO; // Import DTOs
import com.example.ecommerce.ecom_backend.product.dto.ProductResponseDTO;
import com.example.ecommerce.ecom_backend.product.service.ProductService;
import jakarta.validation.Valid; // For @Valid annotation
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // For role-based authorization
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService; // Use final and constructor injection

    @Autowired
    public ProductController(ProductService productService) { // Use constructor injection
        this.productService = productService;
    }

    // Create a new product - Accessible only by ADMIN role
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(@Valid @RequestBody ProductRequestDTO productRequestDTO) {
        ProductResponseDTO newProduct = productService.createProduct(productRequestDTO);
        return ResponseEntity.ok(newProduct); // 201 Created
    }

    // Get all products - Accessible by USER or ADMIN role (or no role if public access is desired)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')") // Or remove @PreAuthorize if public
    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {
        List<ProductResponseDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(products); // 200 OK
    }

    // Get product by ID - Accessible by USER or ADMIN role
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')") // Or remove @PreAuthorize if public
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable Long id) {
        ProductResponseDTO product = productService.getProductById(id); // Service throws NotFoundException
        return ResponseEntity.ok(product); // 200 OK
    }

    // Update an existing product - Accessible only by ADMIN role
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(@PathVariable Long id,
                                                            @Valid @RequestBody ProductRequestDTO productRequestDTO) {
        ProductResponseDTO updatedProduct = productService.updateProduct(id, productRequestDTO);
        return ResponseEntity.ok(updatedProduct); // 200 OK
    }

    // Delete a product - Accessible only by ADMIN role
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id); // Service throws NotFoundException
        return ResponseEntity.ok("Product with ID " + id + " deleted successfully.");
    }
}