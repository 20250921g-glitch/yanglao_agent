package com.care.module.operation.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.care.module.operation.entity.ActivityRegistration;

public interface ActivityRegistrationService extends IService<ActivityRegistration> {
    IPage<ActivityRegistration> getPage(Integer pageNum, Integer pageSize, Long activityId, Integer status);
}
