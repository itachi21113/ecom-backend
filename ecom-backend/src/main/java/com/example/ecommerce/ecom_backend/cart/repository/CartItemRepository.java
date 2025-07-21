package com.example.ecommerce.ecom_backend.cart.repository;

import com.example.ecommerce.ecom_backend.cart.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem , Long> {
}
