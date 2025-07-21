package com.example.ecommerce.ecom_backend.order.model;

import com.example.ecommerce.ecom_backend.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList; // Using ArrayList for List initialization
import java.util.List; // Using List for orderItems


@Entity // Marks this class as a JPA entity
@Table(name = "orders") // Maps this entity to the "orders" table
@Data // Lombok annotation to generate getters, setters, equals, hashCode, and toString
@NoArgsConstructor // Lombok annotation to generate a no-argument constructor
@AllArgsConstructor // Lombok annotation to generate a constructor with all fields
@EntityListeners(AuditingEntityListener.class) // Enables JPA auditing for createdAt and updatedAt
public class Order {


    @Id // Marks this field as the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Configures auto-generation of primary key by the database
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY) // Many orders can belong to one user. FetchType.LAZY avoids loading user unless explicitly accessed.
    @JoinColumn(name = "user_id", nullable = false) // Specifies the foreign key column name
    private User user;


   @Column(nullable = false, updatable = false) // Cannot be null and cannot be updated after creation
    private LocalDateTime orderDate; // Renamed from createdAt for clarity in business context


    @Column(nullable = false)
    private BigDecimal totalAmount;


    @Column(nullable = false)
    private String status;


    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    private List<OrderItem> orderItems = new ArrayList<>(); // Initialize to prevent NullPointerException

    @CreatedDate // Marks this field to be populated with the creation timestamp
    @Column(nullable = false, updatable = false) // Cannot be null and cannot be updated after creation
    private LocalDateTime createdAt;

    @LastModifiedDate // Marks this field to be populated with the last modification timestamp
    @Column(nullable = false) // Cannot be null
    private LocalDateTime updatedAt;

    // Helper method to add an OrderItem to the order
    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }

    // Helper method to remove an OrderItem from the order
    public void removeOrderItem(OrderItem item) {
        orderItems.remove(item);
        item.setOrder(null);
    }
}
