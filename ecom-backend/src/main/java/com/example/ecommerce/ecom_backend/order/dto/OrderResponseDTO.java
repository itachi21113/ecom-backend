// src/main/java/com/example/ecommerce/ecom_backend/order/dto/OrderResponseDTO.java
package com.example.ecommerce.ecom_backend.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {
    private Long id; // Order ID
    private Long userId;
    private String userEmail; // To easily identify the user
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private String status; // Order status (e.g., PENDING, SHIPPED, DELIVERED)
    private List<OrderItemResponseDTO> items; // List of items in the order
}