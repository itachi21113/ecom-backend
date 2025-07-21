package com.example.ecommerce.ecom_backend.order.repository;

import com.example.ecommerce.ecom_backend.order.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    // Custom query methods for OrderItem can be added here, e.g.:
    // List<OrderItem> findByOrder(Order order);
}