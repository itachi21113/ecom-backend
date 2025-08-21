package com.example.ecommerce.ecom_backend.services;

import com.example.ecommerce.ecom_backend.common.exception.ResourceNotFoundException;
import com.example.ecommerce.ecom_backend.product.dto.ProductRequestDTO;
import com.example.ecommerce.ecom_backend.product.dto.ProductResponseDTO;
import com.example.ecommerce.ecom_backend.product.model.Product;
import com.example.ecommerce.ecom_backend.product.repository.ProductRepository;
import com.example.ecommerce.ecom_backend.product.service.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product testProduct;
    private ProductRequestDTO productRequestDTO;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setDescription("A product for testing");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setStockQuantity(50);

        productRequestDTO = new ProductRequestDTO();
        productRequestDTO.setName("New Product");
        productRequestDTO.setDescription("A new product");
        productRequestDTO.setPrice(new BigDecimal("129.99"));
        productRequestDTO.setStockQuantity(100);
    }

    @Test
    void createProduct_shouldReturnSavedProduct() {
        // Arrange
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        ProductResponseDTO result = productService.createProduct(productRequestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testProduct.getId(), result.getId());
        assertEquals(testProduct.getName(), result.getName());
    }

    @Test
    void getAllProducts_shouldReturnListOfProducts() {
        // Arrange
        when(productRepository.findAll()).thenReturn(Collections.singletonList(testProduct));

        // Act
        List<ProductResponseDTO> result = productService.getAllProducts();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProduct.getName(), result.get(0).getName());
    }

    @Test
    void getProductById_shouldReturnProduct_whenFound() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act
        ProductResponseDTO result = productService.getProductById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testProduct.getId(), result.getId());
    }

    @Test
    void getProductById_shouldThrowResourceNotFoundException_whenNotFound() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(1L));
    }

    @Test
    void updateProduct_shouldReturnUpdatedProduct_whenFound() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        ProductResponseDTO result = productService.updateProduct(1L, productRequestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(productRequestDTO.getName(), testProduct.getName());
        assertEquals(productRequestDTO.getPrice(), testProduct.getPrice());
        verify(productRepository, times(1)).save(testProduct);
    }

    @Test
    void deleteProduct_shouldCallDelete_whenProductExists() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        doNothing().when(productRepository).delete(testProduct);

        // Act
        productService.deleteProduct(1L);

        // Assert
        verify(productRepository, times(1)).delete(testProduct);
    }

    @Test
    void deleteProduct_shouldThrowResourceNotFoundException_whenProductNotFound() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productService.deleteProduct(1L));
    }
}