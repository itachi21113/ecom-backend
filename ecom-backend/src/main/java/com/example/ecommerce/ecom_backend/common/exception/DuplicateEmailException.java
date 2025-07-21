// src/main/java/com/example/ecommerce/ecom_backend/exception/DuplicateEmailException.java
package com.example.ecommerce.ecom_backend.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT) // Maps this exception to a 409 Conflict HTTP status
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String message) {
        super(message);
    }
}