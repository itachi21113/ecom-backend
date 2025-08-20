package com.example.ecommerce.ecom_backend.user.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


@Entity // Marks this class as a JPA entity
@Table(name = "users") // Maps this entity to the "users" table
@Data // Lombok annotation to generate getters, setters, equals, hashCode, and toString
@Builder
@NoArgsConstructor // Lombok annotation to generate a no-argument constructor
@AllArgsConstructor // Lombok annotation to generate a constructor with all fields
@EntityListeners(AuditingEntityListener.class)
public class User {


    @Id // Marks this field as the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Configures auto-generation of primary key by the database
    private Long id;


    @Column(nullable = true)
    private String username;


    @Column(nullable = false)
    private String password;


    @Column(nullable = false, unique = true)
    private String email;

    private String firstName;


    private String lastName;


    @ManyToMany(fetch = FetchType.EAGER) // Many-to-Many relationship with Role. FetchType.EAGER loads roles immediately.
    @JoinTable(
            name = "user_roles", // Name of the join table
            joinColumns = @JoinColumn(name = "user_id"), // Column in the join table that refers to the User's primary key
            inverseJoinColumns = @JoinColumn(name = "role_id") // Column in the join table that refers to the Role's primary key
    )
    @EqualsAndHashCode.Exclude
    private Set<Role> roles = new HashSet<>(); // Initialize to prevent NullPointerException

    @CreatedDate // Marks this field to be populated with the creation timestamp
    @Column(nullable = false, updatable = false) // Cannot be null and cannot be updated after creation
    private LocalDateTime createdAt;


    @LastModifiedDate // Marks this field to be populated with the last modification timestamp
    @Column(nullable = false) // Cannot be null
    private LocalDateTime updatedAt;
    // Lombok handles constructors, getters, and setters.
}
