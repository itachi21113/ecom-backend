// src/main/java/com/example/ecommerce/ecom_backend/dto/CartItemResponseDTO.java
package com.example.ecommerce.ecom_backend.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponseDTO {

    private Long id; // CartItem ID
    private Long productId;
    private String productName;
    private String imageUrl; // Image of the product
    private Integer quantity;
    private BigDecimal price; // Price of the product at the time it was added to the cart
    private BigDecimal subtotal; // price * quantity
}