// src/main/java/com/example/ecommerce/ecom_backend/order/dto/OrderItemResponseDTO.java
package com.example.ecommerce.ecom_backend.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponseDTO {
    private Long id; // OrderItem ID
    private Long productId;
    private String productName;
    private String imageUrl; // Product image at time of order
    private Integer quantity;
    private BigDecimal priceAtPurchase; // Price of the product when the order was placed
    private BigDecimal subtotal; // quantity * priceAtPurchase
}