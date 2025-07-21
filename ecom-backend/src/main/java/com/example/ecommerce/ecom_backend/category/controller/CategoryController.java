// src/main/java/com/example/ecommerce/ecom_backend/category/controller/CategoryController.java
package com.example.ecommerce.ecom_backend.category.controller;

import com.example.ecommerce.ecom_backend.category.dto.CategoryRequestDTO;
import com.example.ecommerce.ecom_backend.category.dto.CategoryResponseDTO;
import com.example.ecommerce.ecom_backend.category.service.CategoryService;
import jakarta.validation.Valid; // For @Valid annotation
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // For role-based authorization
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // Marks this class as a REST Controller
@RequestMapping("/api/categories") // Base URL for category-related endpoints
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired // Inject CategoryService dependency via constructor
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping // Maps POST requests to /api/categories
    public ResponseEntity<CategoryResponseDTO> createCategory(@Valid @RequestBody CategoryRequestDTO categoryRequestDTO) {
        CategoryResponseDTO newCategory = categoryService.createCategory(categoryRequestDTO);
        return new ResponseEntity<>(newCategory, HttpStatus.CREATED); // 201 Created
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')") // Or remove @PreAuthorize if public access is desired
    @GetMapping // Maps GET requests to /api/categories
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategories() {
        List<CategoryResponseDTO> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories); // 200 OK
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')") // Or remove @PreAuthorize if public access is desired
    @GetMapping("/{id}") // Maps GET requests to /api/categories/{id}
    public ResponseEntity<CategoryResponseDTO> getCategoryById(@PathVariable Long id) {
        CategoryResponseDTO category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category); // 200 OK
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}") // Maps PUT requests to /api/categories/{id}
    public ResponseEntity<CategoryResponseDTO> updateCategory(@PathVariable Long id,
                                                              @Valid @RequestBody CategoryRequestDTO categoryRequestDTO) {
        CategoryResponseDTO updatedCategory = categoryService.updateCategory(id, categoryRequestDTO);
        return ResponseEntity.ok(updatedCategory); // 200 OK
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}") // Maps DELETE requests to /api/categories/{id}
    public ResponseEntity<String> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return new ResponseEntity<>("Category deleted successfully!", HttpStatus.NO_CONTENT); // 204 No Content
    }
}