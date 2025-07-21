// src/main/java/com.example.ecommerce.ecom_backend.category.service/CategoryServiceImpl.java
package com.example.ecommerce.ecom_backend.category.service;

import com.example.ecommerce.ecom_backend.category.dto.CategoryRequestDTO;
import com.example.ecommerce.ecom_backend.category.dto.CategoryResponseDTO;
import com.example.ecommerce.ecom_backend.category.model.Category;
import com.example.ecommerce.ecom_backend.category.repository.CategoryRepository;
import com.example.ecommerce.ecom_backend.common.exception.DuplicateCategoryNameException;
import com.example.ecommerce.ecom_backend.common.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional // All methods in this class will run within a transaction by default
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // Helper method to convert Category entity to CategoryResponseDTO
    private CategoryResponseDTO convertToDto(Category category) {
        if (category == null) {
            return null;
        }
        CategoryResponseDTO dto = new CategoryResponseDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());
        return dto;
    }

    // Helper method to convert CategoryRequestDTO to Category entity (for creation/update)
    private Category convertToEntity(CategoryRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        return category;
    }

    @Override
    public CategoryResponseDTO createCategory(CategoryRequestDTO categoryRequestDTO) throws DuplicateCategoryNameException {
        // Check for duplicate name before creating
        if (categoryRepository.findByName(categoryRequestDTO.getName()).isPresent()) {
            throw new DuplicateCategoryNameException("Category with name '" + categoryRequestDTO.getName() + "' already exists.");
        }

        Category category = convertToEntity(categoryRequestDTO);
        Category savedCategory = categoryRepository.save(category);
        return convertToDto(savedCategory);
    }

    @Override
    public List<CategoryResponseDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponseDTO getCategoryById(Long id) throws ResourceNotFoundException {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return convertToDto(category);
    }

    @Override
    public CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO categoryRequestDTO) throws ResourceNotFoundException, DuplicateCategoryNameException {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        // Check for duplicate name if the name is being changed and conflicts with another category
        if (!categoryRequestDTO.getName().equals(existingCategory.getName())) {
            if (categoryRepository.findByName(categoryRequestDTO.getName()).isPresent()) {
                throw new DuplicateCategoryNameException("Category with name '" + categoryRequestDTO.getName() + "' already exists.");
            }
        }

        existingCategory.setName(categoryRequestDTO.getName());
        existingCategory.setDescription(categoryRequestDTO.getDescription());

        Category updatedCategory = categoryRepository.save(existingCategory);
        return convertToDto(updatedCategory);
    }

    @Override
    public void deleteCategory(Long id) throws ResourceNotFoundException {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category", "id", id);
        }
        // TODO: Add logic here to handle products associated with this category
        // e.g., set product category to null, or prevent deletion if products exist
        categoryRepository.deleteById(id);
    }
}