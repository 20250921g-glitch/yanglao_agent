package com.care.module.operation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.operation.entity.ActivityField;
import com.care.module.operation.mapper.ActivityFieldMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ActivityFieldService extends ServiceImpl<ActivityFieldMapper, ActivityField> {

    public IPage<ActivityField> getPage(Integer pageNum, Integer pageSize, String keyword) {
        Page<ActivityField> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ActivityField> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            w.like(ActivityField::getLabel, keyword);
        }
        w.orderByDesc(ActivityField::getCreateTime);
        return page(page, w);
    }

    public void saveField(ActivityField f) {
        if (f.getId() == null) save(f); else updateById(f);
    }

    public void updateStatus(Long id, Integer status) {
        ActivityField f = getById(id);
        if (f == null) throw new RuntimeException("字段不存在");
        f.setStatus(status);
        updateById(f);
    }
}
