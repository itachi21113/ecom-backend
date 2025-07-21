// src/main/java/com/example.ecommerce.ecom_backend.category.service/CategoryService.java
package com.example.ecommerce.ecom_backend.category.service;

import com.example.ecommerce.ecom_backend.category.dto.CategoryRequestDTO;
import com.example.ecommerce.ecom_backend.category.dto.CategoryResponseDTO;
import com.example.ecommerce.ecom_backend.common.exception.DuplicateCategoryNameException;
import com.example.ecommerce.ecom_backend.common.exception.ResourceNotFoundException;

import java.util.List;

public interface CategoryService {

    CategoryResponseDTO createCategory(CategoryRequestDTO categoryRequestDTO) throws DuplicateCategoryNameException;

    List<CategoryResponseDTO> getAllCategories();

    CategoryResponseDTO getCategoryById(Long id) throws ResourceNotFoundException;

    CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO categoryRequestDTO) throws ResourceNotFoundException, DuplicateCategoryNameException;

    void deleteCategory(Long id) throws ResourceNotFoundException;
}