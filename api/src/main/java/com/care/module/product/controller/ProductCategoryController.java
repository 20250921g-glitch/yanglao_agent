package com.care.module.product.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.care.common.cache.CacheHelper;
import com.care.common.cache.CacheTtl;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.product.entity.ProductCategory;
import com.care.module.product.service.ProductCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product/category")
public class ProductCategoryController {

    @Autowired
    private ProductCategoryService categoryService;
    @Autowired
    private CacheHelper cacheHelper;

    private static final String CACHE_PREFIX = "product:category:list:";

    @GetMapping("/list")
    public Result<List<ProductCategory>> list(@RequestParam(required = false) String serviceType) {
        String key = CACHE_PREFIX + (serviceType == null || serviceType.isEmpty() ? "all" : serviceType);
        Object cached = cacheHelper.get(key);
        if (cached instanceof List) {
            @SuppressWarnings("unchecked")
            List<ProductCategory> list = (List<ProductCategory>) cached;
            return Result.success(list);
        }
        LambdaQueryWrapper<ProductCategory> wrapper = new LambdaQueryWrapper<>();
        if (serviceType != null && !serviceType.isEmpty()) {
            wrapper.eq(ProductCategory::getServiceType, serviceType);
        }
        wrapper.eq(ProductCategory::getStatus, 1);
        wrapper.orderByAsc(ProductCategory::getSort);
        List<ProductCategory> list = categoryService.list(wrapper);
        cacheHelper.put(key, list, CacheTtl.CATEGORY_LIST);
        return Result.success(list);
    }

    @GetMapping("/page")
    public Result<PageResult<ProductCategory>> page(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String serviceType
    ) {
        Page<ProductCategory> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ProductCategory> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like(ProductCategory::getName, name);
        }
        if (serviceType != null && !serviceType.isEmpty()) {
            wrapper.eq(ProductCategory::getServiceType, serviceType);
        }
        wrapper.orderByAsc(ProductCategory::getSort);
        wrapper.orderByDesc(ProductCategory::getCreateTime);
        categoryService.page(page, wrapper);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/{id}")
    public Result<ProductCategory> getById(@PathVariable Long id) {
        ProductCategory category = categoryService.getById(id);
        return Result.success(category);
    }

    @PostMapping
    public Result<Void> save(@RequestBody ProductCategory category) {
        categoryService.save(category);
        cacheHelper.evictByPrefix(CACHE_PREFIX);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody ProductCategory category) {
        category.setId(id);
        categoryService.updateById(category);
        cacheHelper.evictByPrefix(CACHE_PREFIX);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.removeById(id);
        cacheHelper.evictByPrefix(CACHE_PREFIX);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        ProductCategory category = new ProductCategory();
        category.setId(id);
        category.setStatus(status);
        categoryService.updateById(category);
        cacheHelper.evictByPrefix(CACHE_PREFIX);
        return Result.success();
    }
}
