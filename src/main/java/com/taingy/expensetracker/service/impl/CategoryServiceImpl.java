package com.taingy.expensetracker.service.impl;

import com.taingy.expensetracker.dto.CategoryRequest;
import com.taingy.expensetracker.dto.CategoryResponse;
import com.taingy.expensetracker.mapper.CategoryMapper;
import com.taingy.expensetracker.model.Category;
import com.taingy.expensetracker.repository.CategoryRepository;
import com.taingy.expensetracker.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }


    @Override
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    @Override
    public CategoryResponse findById(UUID id) {
        Category category = categoryRepository.findById(id).orElse(null);
        return categoryMapper.toDto(category);
    }

    @Override
    public CategoryResponse create(CategoryRequest category) {
        Category c = categoryMapper.toEntity(category);
        c = categoryRepository.save(c);
        return categoryMapper.toDto(c);
    }

    @Override
    public CategoryResponse update(UUID id, CategoryRequest category) {
        Category c = categoryMapper.toEntity(category);
        c.setId(id);
        c = categoryRepository.save(c);
        return categoryMapper.toDto(c);
    }

    @Override
    public void delete(UUID id) {
        Category c = categoryRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Category with id: " + id + " does not exist"));

        categoryRepository.delete(c);
    }
}
