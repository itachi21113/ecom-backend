// src/main/java/com/example/ecommerce/ecom_backend/service/ProductServiceImpl.java
package com.example.ecommerce.ecom_backend.product.service;

import com.example.ecommerce.ecom_backend.product.dto.ProductRequestDTO; // Import DTOs
import com.example.ecommerce.ecom_backend.common.exception.ResourceNotFoundException; // Import custom exception
import com.example.ecommerce.ecom_backend.product.dto.ProductResponseDTO;
import com.example.ecommerce.ecom_backend.product.model.Product; // Import Product entity
import com.example.ecommerce.ecom_backend.product.repository.ProductRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors; // For stream operations

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository; // Use final and constructor injection

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository) { // Use constructor injection
        this.productRepository = productRepository;
    }

    // Convert ProductRequestDTO to Product entity
    private Product mapRequestDTOToEntity(ProductRequestDTO dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStockQuantity(dto.getStockQuantity());
        product.setImageUrl(dto.getImageUrl());
        return product;
    }

    // Convert Product entity to ProductResponseDTO
    private ProductResponseDTO mapEntityToResponseDTO(Product product) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setImageUrl(product.getImageUrl());
        return dto;
    }
    // 3. @CacheEvict: Removes data from the cache.
    // - When we create a new product, the 'allProducts' list is now outdated.
    // - This annotation EVICTS (deletes) the 'allProducts' entry from the cache.
    // - The next time getAllProducts() is called, it will be a "cache miss", forcing a fresh fetch from the DB.

    @Override
    @CacheEvict(value = "products", key = "'allProducts'")
    public ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO) {
        Product product = mapRequestDTOToEntity(productRequestDTO);
        Product savedProduct = productRepository.save(product);
        return mapEntityToResponseDTO(savedProduct);
    }

    @Override
    @Cacheable(value = "products", key = "'allProducts'")
    public List<ProductResponseDTO> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(this::mapEntityToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "products", key = "#id")
    public ProductResponseDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return mapEntityToResponseDTO(product);
    }
    // 4. @CachePut: Updates the value in the cache without interfering with the method execution.
    // - When a product is updated, we want to update its specific entry in the cache.

    @Override
    @Caching(
            put = { @CachePut(value = "products", key = "#id") },
            evict = { @CacheEvict(value = "products", key = "'allProducts'") }
    )
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO productRequestDTO) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        existingProduct.setName(productRequestDTO.getName());
        existingProduct.setDescription(productRequestDTO.getDescription());
        existingProduct.setPrice(productRequestDTO.getPrice());
        existingProduct.setStockQuantity(productRequestDTO.getStockQuantity());
        existingProduct.setImageUrl(productRequestDTO.getImageUrl());

        Product updatedProduct = productRepository.save(existingProduct);
        return mapEntityToResponseDTO(updatedProduct);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "products", key = "#id"),
            @CacheEvict(value = "products", key = "'allProducts'")
    })
    public void deleteProduct(Long id) {
        // Check if product exists before deleting
        Product productToDelete = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        productRepository.delete(productToDelete);
    }
}