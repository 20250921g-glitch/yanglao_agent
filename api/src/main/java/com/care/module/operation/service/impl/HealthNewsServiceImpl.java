package com.care.module.operation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.operation.entity.HealthNews;
import com.care.module.operation.mapper.HealthNewsMapper;
import com.care.module.operation.service.HealthNewsService;
import org.springframework.stereotype.Service;

@Service
public class HealthNewsServiceImpl extends ServiceImpl<HealthNewsMapper, HealthNews> implements HealthNewsService {

    @Override
    public IPage<HealthNews> getPage(Integer pageNum, Integer pageSize, String title, String category, Integer status) {
        Page<HealthNews> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<HealthNews> wrapper = new LambdaQueryWrapper<>();
        if (title != null && !title.isEmpty()) wrapper.like(HealthNews::getTitle, title);
        if (category != null && !category.isEmpty()) wrapper.eq(HealthNews::getCategory, category);
        if (status != null) wrapper.eq(HealthNews::getStatus, status);
        wrapper.orderByDesc(HealthNews::getSort).orderByDesc(HealthNews::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public void add(HealthNews news) {
        news.setViewCount(0);
        news.setSort(news.getSort() != null ? news.getSort() : 0);
        save(news);
    }

    @Override
    public void updateNews(HealthNews news) {
        updateById(news);
    }

    @Override
    public void delete(Long id) {
        removeById(id);
    }
}
