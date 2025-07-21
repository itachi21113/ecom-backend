// src/main/java/com/example/ecommerce/ecom_backend/cart/service/CartServiceImpl.java
package com.example.ecommerce.ecom_backend.cart.service;

import com.example.ecommerce.ecom_backend.cart.dto.CartItemRequestDTO;
import com.example.ecommerce.ecom_backend.cart.dto.CartItemResponseDTO;
import com.example.ecommerce.ecom_backend.cart.dto.CartResponseDTO;
import com.example.ecommerce.ecom_backend.common.exception.ResourceNotFoundException;
import com.example.ecommerce.ecom_backend.common.exception.InsufficientStockException; // Our custom exception
import com.example.ecommerce.ecom_backend.cart.model.Cart;
import com.example.ecommerce.ecom_backend.cart.model.CartItem;
import com.example.ecommerce.ecom_backend.product.model.Product; // Product is in a different package
import com.example.ecommerce.ecom_backend.user.model.User;     // User is in a different package
import com.example.ecommerce.ecom_backend.cart.repository.CartItemRepository;
import com.example.ecommerce.ecom_backend.cart.repository.CartRepository;
import com.example.ecommerce.ecom_backend.product.repository.ProductRepository; // ProductRepository is in a different package
import com.example.ecommerce.ecom_backend.user.repository.UserRepository;     // UserRepository is in a different package
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service // Marks this class as a Spring Service component
@Transactional // Ensures atomicity for methods that modify data (all methods in this class will run in a transaction by default)
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository; // We need this to get product details
    private final UserRepository userRepository;     // We need this to get user details

    // Constructor Injection: Spring will automatically provide instances of these repositories
    @Autowired
    public CartServiceImpl(CartRepository cartRepository,
                           CartItemRepository cartItemRepository,
                           ProductRepository productRepository,
                           UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    // --- Helper Methods ---

    /**
     * Helper method to get the ID of the currently authenticated (logged-in) user.
     * It relies on Spring Security's context.
     */
    private Long getCurrentAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated."); // This should ideally be caught by security filters
        }
        String userEmail = authentication.getName(); // Assuming user's email is stored as the principal's name
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email" , userEmail));
        return user.getId();
    }

    /**
     * Helper method to find a cart for a given user ID, or create a new one if it doesn't exist.
     */
    private Cart getOrCreateUserCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> { // If no cart is found, create a new one
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId)); // User must exist
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart); // Save the new cart
                });
    }

    /**
     * Helper method to convert a CartItem entity to a CartItemResponseDTO.
     * This is crucial for sending only necessary data to the client.
     */
    private CartItemResponseDTO mapCartItemToDTO(CartItem cartItem) {
        Product product = cartItem.getProduct();
        // Calculate subtotal for this item (price * quantity)
        BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        return new CartItemResponseDTO(
                cartItem.getId(),
                product.getId(),
                product.getName(),
                product.getImageUrl(),
                cartItem.getQuantity(),
                product.getPrice(), // Price at the time it's mapped, could be current product price
                subtotal
        );
    }

    /**
     * Helper method to convert a Cart entity to a CartResponseDTO.
     * This prepares the entire cart's data for the client.
     */
    private CartResponseDTO mapCartToDTO(Cart cart) {
        // Map each CartItem entity within the cart to its DTO counterpart
        List<CartItemResponseDTO> itemDTOs = cart.getCartItems().stream()
                .map(this::mapCartItemToDTO)
                .collect(Collectors.toList());

        // Calculate the total price of the entire cart by summing up item subtotals
        BigDecimal totalPrice = itemDTOs.stream()
                .map(CartItemResponseDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add); // Start with 0 and add each subtotal

        return new CartResponseDTO(
                cart.getId(),
                cart.getUser().getId(),
                itemDTOs,
                totalPrice
        );
    }

    // --- Service Operations (Implementing CartService Interface Methods) ---

    @Override
    public CartResponseDTO addProductToCart(CartItemRequestDTO cartItemRequestDTO) {
        // 1. Get the current user's ID
        Long userId = getCurrentAuthenticatedUserId();
        // 2. Get or create the user's cart
        Cart cart = getOrCreateUserCart(userId);

        // 3. Find the product by ID
        Product product = productRepository.findById(cartItemRequestDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", cartItemRequestDTO.getProductId()));

        // 4. Basic stock check: Ensure initial requested quantity isn't more than available stock
        if (product.getStockQuantity() < cartItemRequestDTO.getQuantity()) {
            throw new InsufficientStockException("Not enough stock for product: " + product.getName() + ". Available: " + product.getStockQuantity() + ". Requested: " + cartItemRequestDTO.getQuantity());
        }

        // 5. Check if the product already exists in the cart
        Optional<CartItem> existingCartItemOptional = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();

        CartItem cartItem;
        if (existingCartItemOptional.isPresent()) {
            // Product already in cart, update quantity
            cartItem = existingCartItemOptional.get();
            int newQuantity = cartItem.getQuantity() + cartItemRequestDTO.getQuantity();

            // Check stock again for the *combined* quantity
            if (product.getStockQuantity() < newQuantity) {
                throw new InsufficientStockException("Adding " + cartItemRequestDTO.getQuantity() + " more units of " + product.getName() + " would exceed available stock. Available: " + product.getStockQuantity() + ", Current in cart: " + cartItem.getQuantity());
            }
            cartItem.setQuantity(newQuantity);
            cartItem.setPrice(product.getPrice()); // Always update price from current product price
        } else {
            // Product not in cart, create a new CartItem
            cartItem = new CartItem(cart, product, cartItemRequestDTO.getQuantity());
            cartItem.setPrice(product.getPrice()); // Set initial price
            cart.addCartItem(cartItem); // Add to cart and establish bidirectional link
        }

        // 6. Save the cart item. This will cascade and update the Cart if needed.
        cartItemRepository.save(cartItem);

        // 7. Return the updated cart details as a DTO
        return mapCartToDTO(cart);
    }

    @Override
    public CartResponseDTO updateProductQuantityInCart(Long cartItemId, Integer quantity) {
        Long userId = getCurrentAuthenticatedUserId();
        Cart cart = getOrCreateUserCart(userId); // Ensure cart exists for user

        // Find the specific CartItem within the current user's cart
        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getId().equals(cartItemId) )
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", " for current user's cart", cartItemId  ));

        // If quantity is 0 or less, treat it as a removal
        if (quantity <= 0) {
            removeProductFromCart(cartItemId);
            // Re-fetch the cart after removal to ensure it's up-to-date for the response
            cart = getOrCreateUserCart(userId);
            return mapCartToDTO(cart);
        }

        // Check if the new quantity exceeds product stock
        Product product = cartItem.getProduct();
        if (product.getStockQuantity() < quantity) {
            throw new InsufficientStockException("Cannot set quantity to " + quantity + " for product: " + product.getName() + ". Available stock: " + product.getStockQuantity());
        }

        cartItem.setQuantity(quantity);
        cartItem.setPrice(product.getPrice()); // Always update price from current product price
        cartItemRepository.save(cartItem); // Save the updated cart item

        return mapCartToDTO(cart);
    }

    @Override
    public String removeProductFromCart(Long cartItemId) {
        Long userId = getCurrentAuthenticatedUserId();
        Cart cart = getOrCreateUserCart(userId); // Ensure cart exists for user

        // Find the CartItem in the database
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", cartItemId));

        // Important: Verify the cart item belongs to the *current user's* cart
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Cart item with ID " + cartItemId + " does not belong to the current user's cart.");
        }

        // Remove the item from the cart's collection and then delete from repository
        cart.removeCartItem(cartItem); // This updates the bidirectional relationship in the Cart entity
        cartItemRepository.delete(cartItem);

        return "Product with Cart Item ID " + cartItemId + " removed from cart successfully.";
    }

    @Override
    public CartResponseDTO getMyCart() {
        Long userId = getCurrentAuthenticatedUserId();
        // Get or create the cart. This ensures that even if a user has no cart, an empty one is shown.
        Cart cart = getOrCreateUserCart(userId);
        return mapCartToDTO(cart);
    }

    @Override
    public String clearMyCart() {
        Long userId = getCurrentAuthenticatedUserId();
        Cart cart = getOrCreateUserCart(userId);

        if (cart.getCartItems().isEmpty()) {
            return "Cart is already empty.";
        }

        // Clear all items from the cart's collection.
        // Thanks to CascadeType.ALL and orphanRemoval=true on the Cart entity's cartItems list,
        // removing items from the collection will automatically delete them from the database.
        cart.getCartItems().clear();
        cartRepository.save(cart); // Save the cart to persist the changes (i.e., item deletions)

        return "Cart cleared successfully.";
    }
}