// src/main/java/com/example/ecommerce/ecom_backend/dto/ErrorDetails.java
package com.example.ecommerce.ecom_backend.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorDetails {
    private Date timestamp;
    private String message;
    private String details; // e.g., the request URI
}