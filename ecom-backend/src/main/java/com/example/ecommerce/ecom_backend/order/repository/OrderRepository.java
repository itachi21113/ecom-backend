package com.example.ecommerce.ecom_backend.order.repository;

import com.example.ecommerce.ecom_backend.order.model.Order;
import com.example.ecommerce.ecom_backend.user.model.User; // Assuming User model is in this package
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long id);
}