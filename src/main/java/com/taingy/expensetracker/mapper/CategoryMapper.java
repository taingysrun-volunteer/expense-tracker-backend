package com.taingy.expensetracker.mapper;

import com.taingy.expensetracker.dto.CategoryRequest;
import com.taingy.expensetracker.dto.CategoryResponse;
import com.taingy.expensetracker.model.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponse toDto(Category category) {
        if (category == null) return null;

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .isActive(category.getIsActive())
                .build();
    }

    public Category toEntity(CategoryRequest request) {
        if (request == null) return null;

        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIsActive(request.getIsActive());

        return category;
    }
}
