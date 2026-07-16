package com.care.module.operation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.common.cache.CacheHelper;
import com.care.module.operation.entity.Banner;
import com.care.module.operation.mapper.BannerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class BannerService extends ServiceImpl<BannerMapper, Banner> {

    @Autowired
    private CacheHelper cacheHelper;

    public IPage<Banner> getPage(Integer pageNum, Integer pageSize, String position, Integer status) {
        Page<Banner> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Banner> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(position)) wrapper.eq(Banner::getPosition, position);
        if (status != null) wrapper.eq(Banner::getStatus, status);
        wrapper.orderByAsc(Banner::getSort).orderByDesc(Banner::getCreateTime);
        return page(page, wrapper);
    }

    public void add(Banner banner) {
        save(banner);
        cacheHelper.evictByPrefix("banner:page:");
    }

    public void updateBanner(Banner banner) {
        updateById(banner);
        cacheHelper.evictByPrefix("banner:page:");
    }

    public void delete(Long id) {
        removeById(id);
        cacheHelper.evictByPrefix("banner:page:");
    }
}
