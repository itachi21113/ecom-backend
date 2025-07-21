// src/main/java/com/example/ecommerce/ecom_backend/service/CartService.java
package com.example.ecommerce.ecom_backend.cart.service;

import com.example.ecommerce.ecom_backend.cart.dto.CartItemRequestDTO;
import com.example.ecommerce.ecom_backend.cart.dto.CartResponseDTO;

public interface CartService {


    CartResponseDTO addProductToCart(CartItemRequestDTO cartItemRequestDTO);


    CartResponseDTO updateProductQuantityInCart(Long cartItemId, Integer quantity);


    String removeProductFromCart(Long cartItemId);


    CartResponseDTO getMyCart();


    String clearMyCart();
}