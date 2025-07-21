// src/main/java/com/example/ecommerce/ecom_backend/model/Cart.java
package com.example.ecommerce.ecom_backend.cart.model;

import com.example.ecommerce.ecom_backend.user.model.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList; // Or HashSet, depending on preference for collection type
import java.util.List;     // Or Set, depending on preference

@Entity
@Table(name = "carts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

     @OneToOne(fetch = FetchType.LAZY) // LAZY fetch type is generally preferred for performance
     @JoinColumn(name = "user_id", nullable = false, unique = true) // Foreign key column, must be unique for each user
     @EqualsAndHashCode.Exclude // Exclude user from equals and hashCode
     private User user;

    // One-to-Many relationship with CartItem. A Cart can have many CartItems.
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @EqualsAndHashCode.Exclude // Exclude user from equals and hashCode
    private List<CartItem> cartItems = new ArrayList<>(); // Initialize to prevent NullPointerException

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Convenience method to add a CartItem
    public void addCartItem(CartItem item) {
        if (cartItems == null) {
            cartItems = new ArrayList<>();
        }
        cartItems.add(item);
        item.setCart(this); // Ensure the bidirectional relationship is set
    }

    // Convenience method to remove a CartItem
    public void removeCartItem(CartItem item) {
        if (cartItems != null) {
            cartItems.remove(item);
            item.setCart(null); // Remove the bidirectional relationship
        }
    }
}