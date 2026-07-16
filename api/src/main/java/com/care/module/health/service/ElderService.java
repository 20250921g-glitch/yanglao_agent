package com.care.module.health.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.health.entity.Elder;
import com.care.module.health.mapper.ElderMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ElderService extends ServiceImpl<ElderMapper, Elder> {

    public IPage<Elder> getPage(Integer pageNum, Integer pageSize, String name, String phone, Integer healthLevel) {
        Page<Elder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Elder> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(Elder::getName, name);
        }
        if (StringUtils.hasText(phone)) {
            wrapper.eq(Elder::getPhone, phone);
        }
        if (healthLevel != null) {
            wrapper.eq(Elder::getHealthLevel, healthLevel);
        }
        wrapper.orderByDesc(Elder::getCreateTime);
        return page(page, wrapper);
    }

    public List<Elder> getAll() {
        return list(new LambdaQueryWrapper<Elder>().eq(Elder::getStatus, 1));
    }

    public void add(Elder elder) {
        save(elder);
    }

    public void updateElder(Elder elder) {
        updateById(elder);
    }

    public void delete(Long id) {
        removeById(id);
    }
}
