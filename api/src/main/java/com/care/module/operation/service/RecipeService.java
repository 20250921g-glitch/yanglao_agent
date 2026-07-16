package com.care.module.operation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.operation.entity.Recipe;
import com.care.module.operation.mapper.RecipeMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RecipeService extends ServiceImpl<RecipeMapper, Recipe> {

    public IPage<Recipe> getPage(Integer pageNum, Integer pageSize, String category, Integer status) {
        Page<Recipe> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Recipe> wrapper = new LambdaQueryWrapper<>();
        if (category != null && !category.isEmpty()) {
            wrapper.eq(Recipe::getCategory, category);
        }
        if (status != null) {
            wrapper.eq(Recipe::getStatus, status);
        }
        wrapper.orderByDesc(Recipe::getCreateTime);
        IPage<Recipe> result = page(page, wrapper);
        result.getRecords().forEach(this::fillText);
        return result;
    }

    public List<Recipe> getAll(Integer status) {
        LambdaQueryWrapper<Recipe> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Recipe::getStatus, status);
        }
        wrapper.orderByDesc(Recipe::getCreateTime);
        List<Recipe> list = list(wrapper);
        list.forEach(this::fillText);
        return list;
    }

    public void fillText(Recipe recipe) {
        Map<String, String> categoryMap = new HashMap<>();
        categoryMap.put("breakfast", "早餐");
        categoryMap.put("lunch", "午餐");
        categoryMap.put("dinner", "晚餐");
        categoryMap.put("snack", "加餐");
        recipe.setCategoryText(categoryMap.getOrDefault(recipe.getCategory(), recipe.getCategory()));

        if (recipe.getStatus() == 1) {
            recipe.setStatusText("启用");
        } else {
            recipe.setStatusText("禁用");
        }
    }
}