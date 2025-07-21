// src/main/java/com/example.ecommerce.ecom_backend.order.controller/OrderController.java
package com.example.ecommerce.ecom_backend.order.controller;

import com.example.ecommerce.ecom_backend.order.dto.OrderRequestDTO;
import com.example.ecommerce.ecom_backend.order.dto.OrderResponseDTO;
import com.example.ecommerce.ecom_backend.order.service.OrderService;
import jakarta.validation.Valid; // For @Valid annotation
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // For role-based authorization
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders") // Base URL for order-related endpoints
public class OrderController {

    private final OrderService orderService;

    @Autowired // Inject OrderService dependency via constructor
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping // Maps POST requests to /api/orders
    public ResponseEntity<OrderResponseDTO> placeOrder(@Valid @RequestBody OrderRequestDTO orderRequestDTO) {
        OrderResponseDTO newOrder = orderService.placeOrder(orderRequestDTO);
        return new ResponseEntity<>(newOrder, HttpStatus.CREATED); // 201 Created
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/my-orders") // Maps GET requests to /api/orders/my-orders
    public ResponseEntity<List<OrderResponseDTO>> getMyOrders() {
        List<OrderResponseDTO> myOrders = orderService.getMyOrders();
        return ResponseEntity.ok(myOrders); // 200 OK
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{orderId}") // Maps GET requests to /api/orders/{orderId}
    public ResponseEntity<OrderResponseDTO> getOrderDetails(@PathVariable Long orderId) {
        // Service will throw ResourceNotFoundException if not found or not belonging to user
        OrderResponseDTO order = orderService.getOrderDetails(orderId);
        return ResponseEntity.ok(order); // 200 OK
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping // Maps GET requests to /api/orders (without /{orderId} or /my-orders)
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        List<OrderResponseDTO> allOrders = orderService.getAllOrders();
        return ResponseEntity.ok(allOrders); // 200 OK
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{orderId}/status") // Maps PUT requests to /api/orders/{orderId}/status
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String newStatus) { // @RequestParam for query parameter
        OrderResponseDTO updatedOrder = orderService.updateOrderStatus(orderId, newStatus);
        return ResponseEntity.ok(updatedOrder); // 200 OK
    }
}