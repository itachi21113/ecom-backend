// src/main/java/com/example/ecommerce/ecom_backend/controller/TestController.java
package com.example.ecommerce.ecom_backend.auth.service;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test") // This endpoint will be protected by default
public class TestController {

    @GetMapping("/protected")
    public ResponseEntity<String> getProtectedData() {
        return ResponseEntity.ok("You successfully accessed protected data with your JWT!");
    }

    // You can add another one for future role-based testing
    @GetMapping("/admin-only")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> getAdminOnlyData() {
        return ResponseEntity.ok("This data is only for Admins!");
    }
}