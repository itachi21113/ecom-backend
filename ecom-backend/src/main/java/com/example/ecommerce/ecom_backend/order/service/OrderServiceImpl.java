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
@Transactional // All methods in this class will run within a transaction by default
public class OrderServiceImpl implements OrderService {

    // Repositories for Order management
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    // Repositories for dependencies
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    // We'll also use CartService for cart-related operations like clearing it
    private final CartService cartService;


    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderItemRepository orderItemRepository,
                            UserRepository userRepository,
                            ProductRepository productRepository,
                            CartRepository cartRepository,
                            CartItemRepository cartItemRepository,
                            CartService cartService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.cartService = cartService;
    }

    // --- Helper Methods ---

    /**
     * Helper to get the currently authenticated user's ID.
     */
    private Long getCurrentAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated."); // This should be handled by security filters
        }
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));
        return user.getId();
    }

    /**
     * Helper to get the currently authenticated user's User entity.
     */
    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated."); // Should not happen
        }
        String userEmail = authentication.getName();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));
    }

    /**
     * Helper to map OrderItem entity to OrderItemResponseDTO.
     */
    private OrderItemResponseDTO mapOrderItemToDTO(OrderItem orderItem) {
        Product product = orderItem.getProduct();
        BigDecimal subtotal = orderItem.getPriceAtPurchase().multiply(BigDecimal.valueOf(orderItem.getQuantity()));
        return new OrderItemResponseDTO(
                orderItem.getId(),
                product.getId(),
                product.getName(),
                product.getImageUrl(),
                orderItem.getQuantity(),
                orderItem.getPriceAtPurchase(),
                subtotal
        );
    }

    /**
     * Helper to map Order entity to OrderResponseDTO.
     */
    private OrderResponseDTO mapOrderToDTO(Order order) {
        List<OrderItemResponseDTO> itemDTOs = order.getOrderItems().stream()
                .map(this::mapOrderItemToDTO)
                .collect(Collectors.toList());

        BigDecimal totalAmount = itemDTOs.stream()
                .map(OrderItemResponseDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new OrderResponseDTO(
                order.getId(),
                order.getUser().getId(),
                order.getUser().getEmail(), // User email for clarity
                order.getOrderDate(),
                totalAmount, // Calculated total amount
                order.getStatus(),
                itemDTOs
        );
    }

    // --- Service Operations (Implementing OrderService Interface Methods) ---

    @Override
    public OrderResponseDTO placeOrder(OrderRequestDTO orderRequestDTO) { // orderRequestDTO is currently empty
        User currentUser = getCurrentAuthenticatedUser();

        // 1. Get the current user's cart
        Cart userCart = cartRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "user ID", currentUser.getId()));

        if (userCart.getCartItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot place an order for an empty cart.");
        }

        Order newOrder = new Order();
        newOrder.setUser(currentUser);
        newOrder.setOrderDate(LocalDateTime.now()); // Set current order date
        newOrder.setStatus("PENDING"); // Initial status

        BigDecimal orderTotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        // 2. Process each item in the cart
        for (CartItem cartItem : userCart.getCartItems()) {
            Product product = cartItem.getProduct();

            // 2.1. Final stock check
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new InsufficientStockException("Not enough stock for product: " + product.getName() + ". Available: " + product.getStockQuantity() + ". Requested: " + cartItem.getQuantity());
            }

            // 2.2. Deduct stock from Product
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product); // Save updated product stock

            // 2.3. Create OrderItem
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceAtPurchase(product.getPrice()); // Capture price at time of order
            orderItem.setOrder(newOrder); // Link to the new order
            // FIX: Set subtotal before saving
            orderItem.setSubtotal(orderItem.getPriceAtPurchase().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
            orderItems.add(orderItem);

            orderTotal = orderTotal.add(orderItem.getPriceAtPurchase().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
        }

        newOrder.setTotalAmount(orderTotal);
        newOrder.setOrderItems(orderItems); // Link order items to the order

        // 3. Save the new order (this will cascade save order items)
        Order savedOrder = orderRepository.save(newOrder);

        // 4. Clear the user's cart after successful order placement
        cartService.clearMyCart(); // Uses cartService to clear the cart

        return mapOrderToDTO(savedOrder);
    }

    @Override
    public List<OrderResponseDTO> getMyOrders() {
        User currentUser = getCurrentAuthenticatedUser();
        List<Order> orders = orderRepository.findByUserId(currentUser.getId()); // You'll need this method in OrderRepository
        return orders.stream()
                .map(this::mapOrderToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponseDTO getOrderDetails(Long orderId) {
        User currentUser = getCurrentAuthenticatedUser();
        // Find the order by ID and ensure it belongs to the current user
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Order", "id", orderId + " not found for current user.");
        }
        return mapOrderToDTO(order);
    }

    @Override
    public List<OrderResponseDTO> getAllOrders() { // Admin only
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(this::mapOrderToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponseDTO updateOrderStatus(Long orderId, String newStatus) { // Admin only
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        // Basic validation for new status (you might want to use an Enum here)
        if (!isValidOrderStatus(newStatus)) {
            throw new IllegalArgumentException("Invalid order status: " + newStatus);
        }
        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);
        return mapOrderToDTO(updatedOrder);
    }

    // Helper for status validation (consider making this an Enum later)
    private boolean isValidOrderStatus(String status) {
        return status.equals("PENDING") || status.equals("PROCESSING") ||
                status.equals("SHIPPED") || status.equals("DELIVERED") ||
                status.equals("CANCELLED");
    }
}