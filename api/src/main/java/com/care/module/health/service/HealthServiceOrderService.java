package com.care.module.health.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.health.entity.HealthServiceOrder;
import com.care.module.health.mapper.HealthServiceOrderMapper;
import org.springframework.stereotype.Service;

@Service
public class HealthServiceOrderService extends ServiceImpl<HealthServiceOrderMapper, HealthServiceOrder> {

    public IPage<HealthServiceOrder> getPage(Integer pageNum, Integer pageSize, Long elderId, Integer status) {
        Page<HealthServiceOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<HealthServiceOrder> wrapper = new LambdaQueryWrapper<>();
        if (elderId != null) wrapper.eq(HealthServiceOrder::getElderId, elderId);
        if (status != null) wrapper.eq(HealthServiceOrder::getStatus, status);
        wrapper.orderByDesc(HealthServiceOrder::getCreateTime);
        return page(page, wrapper);
    }
}
