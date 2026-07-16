package com.care.module.service.worker.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.service.worker.entity.ServiceWorkerTag;
import com.care.module.service.worker.mapper.ServiceWorkerTagMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ServiceWorkerTagService extends ServiceImpl<ServiceWorkerTagMapper, ServiceWorkerTag> {

    public IPage<ServiceWorkerTag> getPage(Integer pageNum, Integer pageSize, String tagName, String serviceType) {
        Page<ServiceWorkerTag> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ServiceWorkerTag> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(tagName)) {
            wrapper.like(ServiceWorkerTag::getTagName, tagName);
        }
        if (StringUtils.hasText(serviceType)) {
            wrapper.eq(ServiceWorkerTag::getServiceType, serviceType);
        }
        wrapper.orderByDesc(ServiceWorkerTag::getCreateTime);
        return page(page, wrapper);
    }

    public List<ServiceWorkerTag> getList(String serviceType) {
        LambdaQueryWrapper<ServiceWorkerTag> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(serviceType)) {
            wrapper.eq(ServiceWorkerTag::getServiceType, serviceType);
        }
        wrapper.orderByDesc(ServiceWorkerTag::getCreateTime);
        return list(wrapper);
    }

    public void add(ServiceWorkerTag tag) {
        save(tag);
    }

    public void updateTag(ServiceWorkerTag tag) {
        updateById(tag);
    }

    public void delete(Long id) {
        removeById(id);
    }
}
