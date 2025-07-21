// src/main/java/com/example/ecommerce/ecom_backend/model/Product.java
package com.example.ecommerce.ecom_backend.product.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener; // <-- IMPORTANT: Add this import

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity // Marks this class as a JPA entity
@Table(name = "products") // Maps this entity to the "products" table
@Data // Lombok annotation to generate getters, setters, equals, hashCode, and toString
@NoArgsConstructor // Lombok annotation to generate a no-argument constructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class) // <-- IMPORTANT: Add this annotation here
public class Product {


    @Id // Marks this field as the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Configures auto-generation of primary key by the database
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;


    @Column(columnDefinition = "TEXT") // Defines the column as a TEXT type for longer descriptions
    private String description;


    @Column(nullable = false)
    private BigDecimal price;


    @Column(nullable = false)
    private Integer stockQuantity;

    private String imageUrl;

    @CreatedDate // Marks this field to be populated with the creation timestamp
    @Column(nullable = false, updatable = false) // Cannot be null and cannot be updated after creation
    private LocalDateTime createdAt;


    @LastModifiedDate // Marks this field to be populated with the last modification timestamp
    @Column(nullable = false) // Cannot be null
    private LocalDateTime updatedAt;

    // Custom constructor for creating a product, excluding ID and Auditing fields

}