// src/main/java/com/example/ecommerce/ecom_backend/model/CartItem.java
package com.example.ecommerce.ecom_backend.cart.model;

import com.example.ecommerce.ecom_backend.product.model.Product;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cart_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many-to-One relationship with Cart. Many CartItems can belong to one Cart.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false) // Foreign key to the Cart table
    @EqualsAndHashCode.Exclude // Exclude user from equals and hashCode
    private Cart cart;

    // Many-to-One relationship with Product. Many CartItems can reference one Product.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false) // Foreign key to the Product table
    @EqualsAndHashCode.Exclude // Exclude user from equals and hashCode
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal price; // Price at the time of adding to cart (can differ from current product price)

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public CartItem(Cart cart, Product product, Integer quantity) {
        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
        this.price = product.getPrice(); // Set the price from the product at the time of addition
        // createdAt and updatedAt will be automatically managed by @CreatedDate/@LastModifiedDate
        // id will be automatically generated
    }

}