// src/main/java/com/example.ecommerce.ecom_backend.order.service/OrderServiceImpl.java
package com.example.ecommerce.ecom_backend.order.service;

import com.example.ecommerce.ecom_backend.order.dto.OrderRequestDTO;
import com.example.ecommerce.ecom_backend.order.dto.OrderResponseDTO;
import com.example.ecommerce.ecom_backend.order.dto.OrderItemResponseDTO; // Import this

import com.example.ecommerce.ecom_backend.cart.model.Cart; // Import from cart.model
import com.example.ecommerce.ecom_backend.cart.model.CartItem; // Import from cart.model
import com.example.ecommerce.ecom_backend.cart.repository.CartItemRepository; // Import from cart.repository
import com.example.ecommerce.ecom_backend.cart.repository.CartRepository; // Import from cart.repository
import com.example.ecommerce.ecom_backend.cart.service.CartService; // Import CartService

import com.example.ecommerce.ecom_backend.common.exception.ResourceNotFoundException;
import com.example.ecommerce.ecom_backend.common.exception.InsufficientStockException;
import com.example.ecommerce.ecom_backend.product.model.Product; // Product entity from root model package
import com.example.ecommerce.ecom_backend.user.model.User;     // User entity from root model package
import com.example.ecommerce.ecom_backend.order.model.Order; // Order entity from order.model package
import com.example.ecommerce.ecom_backend.order.model.OrderItem; // OrderItem entity from order.model package

import com.example.ecommerce.ecom_backend.order.repository.OrderItemRepository; // OrderItemRepo from order.repository
import com.example.ecommerce.ecom_backend.order.repository.OrderRepository;     // OrderRepo from order.repository
import com.example.ecommerce.ecom_backend.product.repository.ProductRepository; // ProductRepo from root repository package
import com.example.ecommerce.ecom_backend.user.repository.UserRepository;     // UserRepo from root repository package

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartService cartService;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, OrderItemRepository orderItemRepository, UserRepository userRepository, ProductRepository productRepository, CartRepository cartRepository, CartItemRepository cartItemRepository, CartService cartService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.cartService = cartService;
    }

    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));
    }

    private OrderItemResponseDTO mapOrderItemToDTO(OrderItem orderItem) {
        Product product = orderItem.getProduct();
        return new OrderItemResponseDTO(
                orderItem.getId(),
                product.getId(),
                product.getName(),
                product.getImageUrl(),
                orderItem.getQuantity(),
                orderItem.getPriceAtPurchase(),
                orderItem.getSubtotal()
        );
    }

    /**
     * Helper to map Order entity to OrderResponseDTO.
     */
    private OrderResponseDTO mapOrderToDTO(Order order) {
        List<OrderItemResponseDTO> itemDTOs = order.getOrderItems().stream()
                .map(this::mapOrderItemToDTO)
                .collect(Collectors.toList());

        return new OrderResponseDTO(
                order.getId(),
                order.getUser().getId(),
                order.getUser().getEmail(),
                order.getOrderDate(),
                order.getTotalAmount(), // <-- THE FIX: Use the stored total amount directly
                order.getStatus(),
                itemDTOs
        );
    }

    @Override
    public OrderResponseDTO placeOrder(OrderRequestDTO orderRequestDTO) {
        User currentUser = getCurrentAuthenticatedUser();
        Cart userCart = cartRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "user ID", currentUser.getId()));

        if (userCart.getCartItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot place an order for an empty cart.");
        }

        Order newOrder = new Order();
        newOrder.setUser(currentUser);
        newOrder.setOrderDate(LocalDateTime.now());
        newOrder.setStatus("PENDING");

        BigDecimal orderTotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : userCart.getCartItems()) {
            Product product = cartItem.getProduct();
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new InsufficientStockException("Not enough stock for product: " + product.getName());
            }
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceAtPurchase(product.getPrice());
            orderItem.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            orderItem.setOrder(newOrder);
            orderItems.add(orderItem);

            orderTotal = orderTotal.add(orderItem.getSubtotal());
        }

        newOrder.setTotalAmount(orderTotal);
        newOrder.setOrderItems(orderItems);

        Order savedOrder = orderRepository.save(newOrder);
        cartService.clearMyCart();
        return mapOrderToDTO(savedOrder);
    }

    @Override
    public List<OrderResponseDTO> getMyOrders() {
        User currentUser = getCurrentAuthenticatedUser();
        List<Order> orders = orderRepository.findByUserId(currentUser.getId());
        return orders.stream()
                .map(this::mapOrderToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponseDTO getOrderDetails(Long orderId) {
        User currentUser = getCurrentAuthenticatedUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Order", "id", orderId + " not found for current user.");
        }
        return mapOrderToDTO(order);
    }

    @Override
    public List<OrderResponseDTO> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(this::mapOrderToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponseDTO updateOrderStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!isValidOrderStatus(newStatus)) {
            throw new IllegalArgumentException("Invalid order status: " + newStatus);
        }
        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);
        return mapOrderToDTO(updatedOrder);
    }

    private boolean isValidOrderStatus(String status) {
        return status.equals("PENDING") || status.equals("PAID") || status.equals("PROCESSING") ||
                status.equals("SHIPPED") || status.equals("DELIVERED") ||
                status.equals("CANCELLED");
    }
}