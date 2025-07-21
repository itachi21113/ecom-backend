// src/main/java/com/example/ecommerce/ecom_backend/dto/ProductRequestDTO.java
package com.example.ecommerce.ecom_backend.product.dto;

import jakarta.validation.constraints.DecimalMin; // For price validation
import jakarta.validation.constraints.Min;     // For stock quantity validation
import jakarta.validation.constraints.NotBlank; // For non-blank string fields
import jakarta.validation.constraints.NotNull;  // For non-null number fields
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDTO {

    @NotBlank(message = "Product name cannot be empty")
    private String name;

    @NotBlank(message = "Product description cannot be empty")
    private String description;

    @NotNull(message = "Product price cannot be empty")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Stock quantity cannot be empty")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    // imageUrl can be null or empty, so no @NotBlank here unless you want it required
    private String imageUrl;
}