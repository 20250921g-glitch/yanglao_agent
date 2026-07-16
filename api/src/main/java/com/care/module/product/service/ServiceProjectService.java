package com.care.module.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.common.cache.CacheHelper;
import com.care.module.product.entity.ServiceProject;
import com.care.module.product.mapper.ServiceProjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ServiceProjectService extends ServiceImpl<ServiceProjectMapper, ServiceProject> {

    @Autowired
    private CacheHelper cacheHelper;

    public IPage<ServiceProject> getPage(Integer pageNum, Integer pageSize, String keyword, Integer status) {
        Page<ServiceProject> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ServiceProject> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            w.like(ServiceProject::getName, keyword);
        }
        if (status != null) {
            w.eq(ServiceProject::getStatus, status);
        }
        w.orderByDesc(ServiceProject::getCreateTime);
        return page(page, w);
    }

    public void saveProject(ServiceProject p) {
        if (p.getId() == null) save(p); else updateById(p);
        evictCache(p.getId());
    }

    public void updateStatus(Long id, Integer status) {
        ServiceProject p = getById(id);
        if (p == null) throw new RuntimeException("项目不存在");
        p.setStatus(status);
        updateById(p);
        evictCache(id);
    }

    public void removeProject(Long id) {
        removeById(id);
        evictCache(id);
    }

    private void evictCache(Long id) {
        if (id == null) {
            return;
        }
        cacheHelper.evict("service:project:" + id);
        cacheHelper.evictByPrefix("service:project:list");
    }
}
