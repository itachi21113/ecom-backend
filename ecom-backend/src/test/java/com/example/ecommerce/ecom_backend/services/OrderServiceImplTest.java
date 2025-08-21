package com.example.ecommerce.ecom_backend.service;

import com.example.ecommerce.ecom_backend.cart.model.Cart;
import com.example.ecommerce.ecom_backend.cart.model.CartItem;
import com.example.ecommerce.ecom_backend.cart.repository.CartRepository;
import com.example.ecommerce.ecom_backend.cart.service.CartService;
import com.example.ecommerce.ecom_backend.common.exception.InsufficientStockException;
import com.example.ecommerce.ecom_backend.common.exception.ResourceNotFoundException;
import com.example.ecommerce.ecom_backend.order.dto.OrderRequestDTO;
import com.example.ecommerce.ecom_backend.order.dto.OrderResponseDTO;
import com.example.ecommerce.ecom_backend.order.model.Order;
import com.example.ecommerce.ecom_backend.order.repository.OrderRepository;
import com.example.ecommerce.ecom_backend.order.service.OrderServiceImpl;
import com.example.ecommerce.ecom_backend.product.model.Product;
import com.example.ecommerce.ecom_backend.product.repository.ProductRepository;
import com.example.ecommerce.ecom_backend.user.model.User;
import com.example.ecommerce.ecom_backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CartService cartService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User testUser;
    private Product testProduct;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        // Mocking the security context
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser@example.com");
        SecurityContextHolder.setContext(securityContext);

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("testuser@example.com");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(BigDecimal.valueOf(100));
        testProduct.setStockQuantity(10);

        testCart = new Cart();
        testCart.setId(1L);
        testCart.setUser(testUser);

        when(userRepository.findByEmail("testuser@example.com")).thenReturn(Optional.of(testUser));
    }

    @Test
    void placeOrder_shouldCreateOrder_whenCartIsNotEmpty() {
        // Arrange
        CartItem cartItem = new CartItem(testCart, testProduct, 2);
        testCart.addCartItem(cartItem);

        when(cartRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testCart));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderRequestDTO request = new OrderRequestDTO();

        // Act
        OrderResponseDTO result = orderService.placeOrder(request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(testUser.getId(), result.getUserId());
        assertEquals("PENDING", result.getStatus());
        assertEquals(8, testProduct.getStockQuantity()); // 10 - 2 = 8
        verify(productRepository, times(1)).save(testProduct);
        verify(cartService, times(1)).clearMyCart();
    }

    @Test
    void placeOrder_shouldThrowException_whenCartIsEmpty() {
        // Arrange
        testCart.setCartItems(Collections.emptyList());
        when(cartRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testCart));
        OrderRequestDTO request = new OrderRequestDTO();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> orderService.placeOrder(request));
    }

    @Test
    void placeOrder_shouldThrowInsufficientStockException_whenStockIsTooLow() {
        // Arrange
        CartItem cartItem = new CartItem(testCart, testProduct, 15); // Request more than in stock
        testCart.addCartItem(cartItem);
        when(cartRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testCart));
        OrderRequestDTO request = new OrderRequestDTO();

        // Act & Assert
        assertThrows(InsufficientStockException.class, () -> orderService.placeOrder(request));
    }

    @Test
    void getOrderDetails_shouldReturnOrder_whenOrderBelongsToUser() {
        // Arrange
        Order order = new Order();
        order.setId(1L);
        order.setUser(testUser);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act
        OrderResponseDTO result = orderService.getOrderDetails(1L);

        // Assert
        assertNotNull(result);
        assertEquals(order.getId(), result.getId());
    }

    @Test
    void getOrderDetails_shouldThrowException_whenOrderNotBelongingToUser() {
        // Arrange
        User anotherUser = new User();
        anotherUser.setId(2L);
        Order order = new Order();
        order.setId(1L);
        order.setUser(anotherUser); // Order belongs to a different user
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderDetails(1L));
    }
}