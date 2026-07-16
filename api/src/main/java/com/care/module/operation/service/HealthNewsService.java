package com.care.module.operation.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.care.module.operation.entity.HealthNews;

public interface HealthNewsService extends IService<HealthNews> {
    IPage<HealthNews> getPage(Integer pageNum, Integer pageSize, String title, String category, Integer status);
    void add(HealthNews news);
    void updateNews(HealthNews news);
    void delete(Long id);
}
