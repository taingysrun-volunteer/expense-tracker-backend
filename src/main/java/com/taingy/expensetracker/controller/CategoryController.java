package com.taingy.expensetracker.controller;

import com.taingy.expensetracker.dto.CategoryRequest;
import com.taingy.expensetracker.dto.CategoryResponse;
import com.taingy.expensetracker.dto.ListResponse;
import com.taingy.expensetracker.dto.ResponseMessage;
import com.taingy.expensetracker.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<ListResponse<CategoryResponse>> getCategories() {
        List<CategoryResponse> list = categoryService.findAll();
        ListResponse<CategoryResponse> response = new ListResponse<>(list, list.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategory(@PathVariable UUID id) {
        CategoryResponse categoryResponse = categoryService.findById(id);
        return ResponseEntity.ok(categoryResponse);

    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CategoryRequest categoryRequest) {
        CategoryResponse categoryResponse = categoryService.create(categoryRequest);
        return ResponseEntity.ok(categoryResponse);

    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updaetCategory(@PathVariable UUID id, @RequestBody CategoryRequest categoryRequest) {
        CategoryResponse categoryResponse = categoryService.update(id, categoryRequest);
        return ResponseEntity.ok(categoryResponse);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseMessage> deleteCategory(@PathVariable UUID id) {
        categoryService.delete(id);
        return ResponseEntity.ok(new ResponseMessage("Successfully deleted the category with id: " + id));

    }
}
