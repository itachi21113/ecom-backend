package com.example.ecommerce.ecom_backend.service;

import com.example.ecommerce.ecom_backend.cart.dto.CartItemRequestDTO;
import com.example.ecommerce.ecom_backend.cart.dto.CartResponseDTO;
import com.example.ecommerce.ecom_backend.cart.model.Cart;
import com.example.ecommerce.ecom_backend.cart.model.CartItem;
import com.example.ecommerce.ecom_backend.cart.repository.CartItemRepository;
import com.example.ecommerce.ecom_backend.cart.repository.CartRepository;
import com.example.ecommerce.ecom_backend.cart.service.CartServiceImpl;
import com.example.ecommerce.ecom_backend.common.exception.InsufficientStockException;
import com.example.ecommerce.ecom_backend.common.exception.ResourceNotFoundException;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private User testUser;
    private Product testProduct;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        // Mocking the security context to simulate a logged-in user
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

        // Mock the repository calls that happen in the service
        when(userRepository.findByEmail("testuser@example.com")).thenReturn(Optional.of(testUser));
        // *** THE FIX: Mock the findById call as well ***
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

    }

    @Test
    void addProductToCart_shouldAddNewItem_whenProductNotInCart() {
        // Arrange
        when(SecurityContextHolder.getContext().getAuthentication().getName()).thenReturn(testUser.getEmail());
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(productRepository.findById(testProduct.getId())).thenReturn(Optional.of(testProduct));
        when(cartRepository.findByUserId(testUser.getId())).thenReturn(Optional.empty());

        Cart newCart = new Cart();
        newCart.setUser(testUser);
        when(cartRepository.save(any(Cart.class))).thenReturn(newCart);

        CartItemRequestDTO request = new CartItemRequestDTO(testProduct.getId(), 2);

        // Act
        CartResponseDTO result = cartService.addProductToCart(request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(testProduct.getId(), result.getItems().get(0).getProductId());
        assertEquals(2, result.getItems().get(0).getQuantity());
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }



    @Test
    void addProductToCart_shouldThrowInsufficientStockException_whenStockIsLow() {
        // Arrange
        testProduct.setStockQuantity(1); // Set stock lower than requested quantity
        when(SecurityContextHolder.getContext().getAuthentication().getName()).thenReturn(testUser.getEmail());
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(productRepository.findById(testProduct.getId())).thenReturn(Optional.of(testProduct));
        when(cartRepository.findByUserId(testUser.getId())).thenReturn(Optional.empty());

        CartItemRequestDTO request = new CartItemRequestDTO(testProduct.getId(), 2);

        // Act & Assert
        assertThrows(InsufficientStockException.class, () -> {
            cartService.addProductToCart(request);
        });
    }

    @Test
    void addProductToCart_shouldThrowResourceNotFoundException_whenProductNotFound() {
        // Arrange
        when(SecurityContextHolder.getContext().getAuthentication().getName()).thenReturn(testUser.getEmail());
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty()); // Product does not exist
        when(cartRepository.findByUserId(testUser.getId())).thenReturn(Optional.empty());

        CartItemRequestDTO request = new CartItemRequestDTO(99L, 1); // Non-existent product ID

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            cartService.addProductToCart(request);
        });
    }
}