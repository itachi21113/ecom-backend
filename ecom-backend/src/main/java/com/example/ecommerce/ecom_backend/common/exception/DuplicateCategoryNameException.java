// src/main/java/com/example.ecommerce.ecom_backend.exception/DuplicateCategoryNameException.java
package com.example.ecommerce.ecom_backend.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT) // This maps to HTTP 409 Conflict
public class DuplicateCategoryNameException extends RuntimeException {
    public DuplicateCategoryNameException(String message) {
        super(message);
    }
}