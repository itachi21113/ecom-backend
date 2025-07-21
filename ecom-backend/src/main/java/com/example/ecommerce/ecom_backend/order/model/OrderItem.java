package com.example.ecommerce.ecom_backend.order.model;

import com.example.ecommerce.ecom_backend.product.model.Product;
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


@Entity // Marks this class as a JPA entity
@Table(name = "order_items") // Maps this entity to the "order_items" table
@Data // Lombok annotation to generate getters, setters, equals, hashCode, and toString
@NoArgsConstructor // Lombok annotation to generate a no-argument constructor
@AllArgsConstructor // Lombok annotation to generate a constructor with all fields
@EntityListeners(AuditingEntityListener.class) // Enables JPA auditing for createdAt and updatedAt
public class OrderItem {


    @Id // Marks this field as the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Configures auto-generation of primary key by the database
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Many order items can belong to one order. FetchType.LAZY avoids loading order unless explicitly accessed.
    @JoinColumn(name = "order_id", nullable = false) // Specifies the foreign key column name
    @EqualsAndHashCode.Exclude
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY) // Many order items can reference one product. FetchType.LAZY avoids loading product unless explicitly accessed.
    @JoinColumn(name = "product_id", nullable = false) // Specifies the foreign key column name
    @EqualsAndHashCode.Exclude
    private Product product;


    @Column(nullable = false)
    private Integer quantity;


    @Column(nullable = false)
    private BigDecimal subtotal;

    @Column(nullable = false)
    private BigDecimal priceAtPurchase;


    @CreatedDate // Marks this field to be populated with the creation timestamp
    @Column(nullable = false, updatable = false) // Cannot be null and cannot be updated after creation
    private LocalDateTime createdAt;


    @LastModifiedDate // Marks this field to be populated with the last modification timestamp
    @Column(nullable = false) // Cannot be null
    private LocalDateTime updatedAt;

    // Lombok handles constructors, getters, and setters.
}
