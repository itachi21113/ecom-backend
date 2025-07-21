package com.example.ecommerce.ecom_backend.cart.controller;

import com.example.ecommerce.ecom_backend.cart.dto.CartItemRequestDTO;
import com.example.ecommerce.ecom_backend.cart.dto.CartResponseDTO;
import com.example.ecommerce.ecom_backend.cart.service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController // Marks this class as a REST Controller
@RequestMapping("/api/cart") // Base path for all cart-related endpoints
public class CartController {

    private final CartService cartService;

    @Autowired // Inject CartService dependency via constructor
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')") // Only authenticated users can add items to their cart
    @PostMapping("/items")
    public ResponseEntity<CartResponseDTO> addProductToCart(@Valid @RequestBody CartItemRequestDTO cartItemRequestDTO) {
        CartResponseDTO updatedCart = cartService.addProductToCart(cartItemRequestDTO);
        return new ResponseEntity<>(updatedCart, HttpStatus.OK); // Or HttpStatus.CREATED if a new item is always created
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponseDTO> updateCartItemQuantity(
            @PathVariable Long cartItemId,
            @RequestParam @Valid Integer quantity) { // @Valid for quantity as it's a direct parameter
        CartResponseDTO updatedCart = cartService.updateProductQuantityInCart(cartItemId, quantity);
        return new ResponseEntity<>(updatedCart, HttpStatus.OK);
    }


    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<String> removeProductFromCart(@PathVariable Long cartItemId) {
        String message = cartService.removeProductFromCart(cartItemId);
        return new ResponseEntity<>(message, HttpStatus.OK); // Or HttpStatus.NO_CONTENT for successful deletion
    }


    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<CartResponseDTO> getMyCart() {
        CartResponseDTO cart = cartService.getMyCart();
        return new ResponseEntity<>(cart, HttpStatus.OK);
    }


    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @DeleteMapping("/clear")
    public ResponseEntity<String> clearMyCart() {
        String message = cartService.clearMyCart();
        return new ResponseEntity<>(message, HttpStatus.OK); // HttpStatus.OK is fine for informational success messages
    }
}