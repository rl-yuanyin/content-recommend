package com.cr.image.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cr.common.constant.CommonConstants;
import com.cr.common.result.Result;
import com.cr.common.result.ResultCode;
import com.cr.image.entity.Category;
import com.cr.image.entity.Image;
import com.cr.image.mapper.CategoryMapper;
import com.cr.image.mapper.ImageMapper;
import com.cr.image.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Default image category service implementation.
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    private final ImageMapper imageMapper;

    /**
     * Adds an enabled image category.
     *
     * @param name category name
     * @param sort display order
     * @return new category
     */
    @Override
    public Result<Category> addCategory(String name, Integer sort) {
        if (StrUtil.isBlank(name)) {
            return Result.fail(ResultCode.BAD_REQUEST.getCode(), "分类名称不能为空");
        }
        if (categoryMapper.selectCount(new LambdaQueryWrapper<Category>()
                .eq(Category::getName, name)) > 0) {
            return Result.fail("分类名称已存在");
        }

        Category category = new Category();
        category.setName(name);
        category.setSort(sort == null ? 0 : sort);
        category.setStatus(CommonConstants.STATUS_ENABLED);
        category.setCreateTime(LocalDateTime.now());
        return categoryMapper.insert(category) == 1
                ? Result.success(category)
                : Result.fail("新增分类失败");
    }

    /**
     * Updates category information.
     *
     * @param id category identifier
     * @param name category name
     * @param sort display order
     * @return update result
     */
    @Override
    public Result<Void> updateCategory(Long id, String name, Integer sort) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            return Result.fail(ResultCode.NOT_FOUND.getCode(), "分类不存在");
        }
        if (StrUtil.isNotBlank(name)
                && categoryMapper.selectCount(new LambdaQueryWrapper<Category>()
                .eq(Category::getName, name)
                .ne(Category::getId, id)) > 0) {
            return Result.fail("分类名称已存在");
        }

        if (StrUtil.isNotBlank(name)) {
            category.setName(name);
        }
        if (sort != null) {
            category.setSort(sort);
        }
        return categoryMapper.updateById(category) == 1
                ? Result.success()
                : Result.fail("更新分类失败");
    }

    /**
     * Deletes a category when no images reference it.
     *
     * @param id category identifier
     * @return deletion result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteCategory(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            return Result.fail(ResultCode.NOT_FOUND.getCode(), "分类不存在");
        }
        if (imageMapper.selectCount(new LambdaQueryWrapper<Image>()
                .eq(Image::getCategoryId, id)) > 0) {
            return Result.fail("分类下存在图片，无法删除");
        }
        categoryMapper.deleteById(id);
        return Result.success();
    }

    /**
     * Gets all enabled image categories.
     *
     * @return category list
     */
    @Override
    public Result<List<Category>> getCategoryList() {
        return Result.success(categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getStatus, CommonConstants.STATUS_ENABLED)
                        .orderByAsc(Category::getSort)
                        .orderByAsc(Category::getId)
        ));
    }
}
