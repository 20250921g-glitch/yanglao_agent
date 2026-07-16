package com.care.module.operation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.operation.entity.Food;
import com.care.module.operation.mapper.FoodMapper;
import org.springframework.stereotype.Service;

@Service
public class FoodService extends ServiceImpl<FoodMapper, Food> {

    public IPage<Food> getPage(Integer pageNum, Integer pageSize, String name, String category) {
        Page<Food> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Food> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like(Food::getName, name);
        }
        if (category != null && !category.isEmpty()) {
            wrapper.eq(Food::getCategory, category);
        }
        wrapper.orderByDesc(Food::getCreateTime);
        return page(page, wrapper);
    }

    public void add(Food food) {
        save(food);
    }

    public void updateFood(Food food) {
        updateById(food);
    }

    public void delete(Long id) {
        removeById(id);
    }
}
