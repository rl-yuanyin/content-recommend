package com.cr.image.controller;

import com.cr.common.result.Result;
import com.cr.image.entity.Category;
import com.cr.image.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Image category API.
 */
@RestController
@RequestMapping("/api/image/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Adds a category.
     *
     * @param name category name
     * @param sort display order
     * @return new category
     */
    @PostMapping("/add")
    public Result<Category> addCategory(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") Integer sort) {
        return categoryService.addCategory(name, sort);
    }

    /**
     * Updates a category.
     *
     * @param id category identifier
     * @param name category name
     * @param sort display order
     * @return update result
     */
    @PutMapping("/update")
    public Result<Void> updateCategory(
            @RequestParam Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer sort) {
        return categoryService.updateCategory(id, name, sort);
    }

    /**
     * Deletes a category.
     *
     * @param id category identifier
     * @return deletion result
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        return categoryService.deleteCategory(id);
    }

    /**
     * Gets all enabled image categories.
     *
     * @return category list
     */
    @GetMapping("/list")
    public Result<List<Category>> getCategoryList() {
        return categoryService.getCategoryList();
    }
}
