package com.care.module.operation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.operation.entity.ActivityRegistration;
import com.care.module.operation.mapper.ActivityRegistrationMapper;
import com.care.module.operation.service.ActivityRegistrationService;
import org.springframework.stereotype.Service;

@Service
public class ActivityRegistrationServiceImpl extends ServiceImpl<ActivityRegistrationMapper, ActivityRegistration> implements ActivityRegistrationService {

    @Override
    public IPage<ActivityRegistration> getPage(Integer pageNum, Integer pageSize, Long activityId, Integer status) {
        Page<ActivityRegistration> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ActivityRegistration> wrapper = new LambdaQueryWrapper<>();
        if (activityId != null) wrapper.eq(ActivityRegistration::getActivityId, activityId);
        if (status != null) wrapper.eq(ActivityRegistration::getStatus, status);
        wrapper.orderByDesc(ActivityRegistration::getCreateTime);
        return page(page, wrapper);
    }
}
