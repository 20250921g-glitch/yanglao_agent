package com.care.module.operation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.operation.entity.Activity;
import com.care.module.operation.mapper.ActivityMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ActivityService extends ServiceImpl<ActivityMapper, Activity> {

    public IPage<Activity> getPage(Integer pageNum, Integer pageSize, String name, Integer status,
                                   String category, String location, String startUpdateTime, String endUpdateTime) {
        Page<Activity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) wrapper.like(Activity::getName, name);
        if (status != null) wrapper.eq(Activity::getStatus, status);
        if (StringUtils.hasText(category)) wrapper.eq(Activity::getType, category);
        if (StringUtils.hasText(location)) wrapper.like(Activity::getLocation, location);
        if (StringUtils.hasText(startUpdateTime)) wrapper.ge(Activity::getUpdateTime, startUpdateTime);
        if (StringUtils.hasText(endUpdateTime)) wrapper.le(Activity::getUpdateTime, endUpdateTime);
        wrapper.orderByDesc(Activity::getCreateTime);
        return page(page, wrapper);
    }

    public void add(Activity activity) {
        activity.setUpdateBy(currentOperator());
        save(activity);
    }
    public void updateActivity(Activity activity) {
        activity.setUpdateBy(currentOperator());
        updateById(activity);
    }

    private String currentOperator() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getName() != null && !authentication.getName().isEmpty()) {
                return authentication.getName();
            }
        } catch (Exception ignored) {}
        return "admin";
    }
    public void delete(Long id) { removeById(id); }
}
