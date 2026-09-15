package com.cr.image.service;

import com.cr.common.result.Result;
import com.cr.image.entity.Category;

import java.util.List;

/**
 * Image category service.
 */
public interface CategoryService {

    /**
     * Adds a category.
     *
     * @param name category name
     * @param sort display order
     * @return new category
     */
    Result<Category> addCategory(String name, Integer sort);

    /**
     * Updates a category.
     *
     * @param id category identifier
     * @param name category name
     * @param sort display order
     * @return update result
     */
    Result<Void> updateCategory(Long id, String name, Integer sort);

    /**
     * Deletes a category.
     *
     * @param id category identifier
     * @return deletion result
     */
    Result<Void> deleteCategory(Long id);

    /**
     * Gets all enabled categories.
     *
     * @return category list
     */
    Result<List<Category>> getCategoryList();
}
