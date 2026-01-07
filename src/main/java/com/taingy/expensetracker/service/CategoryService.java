package com.taingy.expensetracker.service;

import com.taingy.expensetracker.dto.CategoryRequest;
import com.taingy.expensetracker.dto.CategoryResponse;

import java.util.List;
import java.util.UUID;

public interface CategoryService {

    List<CategoryResponse> findAll();
    CategoryResponse findById(UUID id);
    CategoryResponse create(CategoryRequest category);
    CategoryResponse update(UUID id, CategoryRequest category);
    void delete(UUID id);

}
