package com.example.ecommerce.ecom_backend.cart.service;

import com.example.ecommerce.ecom_backend.cart.dto.CartItemRequestDTO;
import com.example.ecommerce.ecom_backend.cart.dto.CartItemResponseDTO;
import com.example.ecommerce.ecom_backend.cart.dto.CartResponseDTO;
import com.example.ecommerce.ecom_backend.common.exception.ResourceNotFoundException;
import com.example.ecommerce.ecom_backend.common.exception.InsufficientStockException;
import com.example.ecommerce.ecom_backend.cart.model.Cart;
import com.example.ecommerce.ecom_backend.cart.model.CartItem;
import com.example.ecommerce.ecom_backend.product.model.Product;
import com.example.ecommerce.ecom_backend.user.model.User;
import com.example.ecommerce.ecom_backend.cart.repository.CartItemRepository;
import com.example.ecommerce.ecom_backend.cart.repository.CartRepository;
import com.example.ecommerce.ecom_backend.product.repository.ProductRepository;
import com.example.ecommerce.ecom_backend.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private Cart getOrCreateUserCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
    }

    private CartResponseDTO mapCartToDTO(Cart cart) {
        CartResponseDTO cartDTO = new CartResponseDTO();
        cartDTO.setId(cart.getId());

        List<CartItemResponseDTO> itemDTOs = cart.getCartItems() == null ? Collections.emptyList() :
                cart.getCartItems().stream()
                        .map(this::mapCartItemToDTO)
                        .collect(Collectors.toList());
        cartDTO.setItems(itemDTOs);

        BigDecimal totalPrice = itemDTOs.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cartDTO.setTotalPrice(totalPrice);

        return cartDTO;
    }

    private CartItemResponseDTO mapCartItemToDTO(CartItem cartItem) {
        CartItemResponseDTO itemDTO = new CartItemResponseDTO();
        itemDTO.setId(cartItem.getId());
        itemDTO.setProductId(cartItem.getProduct().getId());
        itemDTO.setQuantity(cartItem.getQuantity());
        itemDTO.setPrice(cartItem.getPrice());
        return itemDTO;
    }

    @Override
    @Transactional
    public CartResponseDTO addProductToCart(CartItemRequestDTO cartItemRequestDTO) {
        User user = getCurrentAuthenticatedUser();
        Cart cart = getOrCreateUserCart(user);
        Long productId = cartItemRequestDTO.getProductId();
        int quantity = cartItemRequestDTO.getQuantity();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        Optional<CartItem> existingItemOpt = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem itemToUpdate = existingItemOpt.get();
            int newQuantity = itemToUpdate.getQuantity() + quantity;
            if (product.getStockQuantity() < newQuantity) {
                throw new InsufficientStockException("Not enough stock for product: " + product.getName());
            }
            itemToUpdate.setQuantity(newQuantity);
        } else {
            if (product.getStockQuantity() < quantity) {
                throw new InsufficientStockException("Not enough stock for product: " + product.getName());
            }
            CartItem newCartItem = new CartItem(cart, product, quantity);
            cart.addCartItem(newCartItem);
        }

        // --- THE CRITICAL FIX ---
        // This line saves the cart and all its items, fixing the "empty cart" error.
        Cart savedCart = cartRepository.save(cart);
        // ---------------------

        return mapCartToDTO(savedCart);
    }

    @Override
    @Transactional
    public CartResponseDTO updateProductQuantityInCart(Long cartItemId, Integer quantity) {
        User user = getCurrentAuthenticatedUser();
        Cart cart = getOrCreateUserCart(user);

        CartItem itemToUpdate = cart.getCartItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));

        if (quantity <= 0) {
            cart.removeCartItem(itemToUpdate);
        } else {
            if (itemToUpdate.getProduct().getStockQuantity() < quantity) {
                throw new InsufficientStockException("Not enough stock for product: " + itemToUpdate.getProduct().getName());
            }
            itemToUpdate.setQuantity(quantity);
        }

        Cart savedCart = cartRepository.save(cart);
        return mapCartToDTO(savedCart);
    }

    @Override
    @Transactional
    public String removeProductFromCart(Long cartItemId) {
        User user = getCurrentAuthenticatedUser();
        Cart cart = getOrCreateUserCart(user);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));

        if (!cartItem.getCart().getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Cart item does not belong to the current user's cart.");
        }

        cart.removeCartItem(cartItem);
        cartRepository.save(cart); // This save is important to trigger orphanRemoval

        return "Product removed from cart successfully.";
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponseDTO getMyCart() {
        User user = getCurrentAuthenticatedUser();
        Cart cart = getOrCreateUserCart(user);
        return mapCartToDTO(cart);
    }

    @Override
    @Transactional
    public String clearMyCart() {
        User user = getCurrentAuthenticatedUser();
        Cart cart = getOrCreateUserCart(user);

        if (cart.getCartItems().isEmpty()) {
            return "Cart is already empty.";
        }

        cart.getCartItems().clear();
        cartRepository.save(cart);

        return "Cart cleared successfully.";
    }
}