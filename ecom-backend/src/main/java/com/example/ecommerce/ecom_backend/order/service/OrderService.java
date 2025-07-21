// src/main/java/com/example.ecommerce.ecom_backend.order.service/OrderService.java
package com.example.ecommerce.ecom_backend.order.service;

import com.example.ecommerce.ecom_backend.order.dto.OrderRequestDTO;
import com.example.ecommerce.ecom_backend.order.dto.OrderResponseDTO;
import java.util.List;

public interface OrderService {


    OrderResponseDTO placeOrder(OrderRequestDTO orderRequestDTO);

    List<OrderResponseDTO> getMyOrders();

    OrderResponseDTO getOrderDetails(Long orderId);

    List<OrderResponseDTO> getAllOrders();

    OrderResponseDTO updateOrderStatus(Long orderId, String newStatus);
}