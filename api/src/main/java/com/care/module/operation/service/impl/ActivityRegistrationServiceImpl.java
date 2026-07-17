package com.care.module.operation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.operation.entity.Activity;
import com.care.module.operation.entity.ActivityRegistration;
import com.care.module.operation.mapper.ActivityRegistrationMapper;
import com.care.module.operation.service.ActivityRegistrationService;
import com.care.module.operation.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ActivityRegistrationServiceImpl extends ServiceImpl<ActivityRegistrationMapper, ActivityRegistration> implements ActivityRegistrationService {

    @Autowired
    private ActivityService activityService;

    @Override
    public IPage<ActivityRegistration> getPage(Integer pageNum, Integer pageSize, Long activityId, Integer status) {
        Page<ActivityRegistration> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ActivityRegistration> wrapper = new LambdaQueryWrapper<>();
        if (activityId != null) wrapper.eq(ActivityRegistration::getActivityId, activityId);
        if (status != null) wrapper.eq(ActivityRegistration::getStatus, status);
        wrapper.orderByDesc(ActivityRegistration::getCreateTime);
        IPage<ActivityRegistration> result = page(page, wrapper);
        // 以 activity 表当前活动名为准，实时回填，避免依赖报名时存储的快照（活动改名后快照会过期）
        List<ActivityRegistration> records = result.getRecords();
        if (records != null && !records.isEmpty()) {
            Set<Long> activityIds = records.stream()
                    .map(ActivityRegistration::getActivityId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (!activityIds.isEmpty()) {
                Map<Long, String> nameMap = activityService.listByIds(activityIds).stream()
                        .collect(Collectors.toMap(Activity::getId, Activity::getName));
                for (ActivityRegistration r : records) {
                    if (r.getActivityId() != null) {
                        r.setActivityName(nameMap.get(r.getActivityId()));
                    }
                }
            }
        }
        return result;
    }
}
