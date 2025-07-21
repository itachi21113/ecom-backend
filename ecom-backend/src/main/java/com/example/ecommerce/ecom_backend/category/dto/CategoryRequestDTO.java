// src/main/java/com/example/ecommerce/ecom_backend/category/dto/CategoryRequestDTO.java
package com.example.ecommerce.ecom_backend.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequestDTO {

    @NotBlank(message = "Category name cannot be empty")
    @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
    private String name;

    @Size(max = 500, message = "Category description cannot exceed 500 characters")
    private String description; // Description can be null or empty
}