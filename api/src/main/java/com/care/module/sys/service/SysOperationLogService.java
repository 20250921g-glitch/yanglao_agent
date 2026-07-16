package com.care.module.sys.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.sys.entity.SysOperationLog;
import com.care.module.sys.mapper.SysOperationLogMapper;
import org.springframework.stereotype.Service;

@Service
public class SysOperationLogService extends ServiceImpl<SysOperationLogMapper, SysOperationLog> {

    public IPage<SysOperationLog> pageQuery(Integer pageNum, Integer pageSize,
                                           String userName, String module,
                                           String operation, String startTime, String endTime) {
        Page<SysOperationLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<SysOperationLog>();
        if (userName != null && !userName.isEmpty()) wrapper.like(SysOperationLog::getUserName, userName);
        if (module != null && !module.isEmpty()) wrapper.eq(SysOperationLog::getModule, module);
        if (operation != null && !operation.isEmpty()) wrapper.like(SysOperationLog::getOperation, operation);
        if (startTime != null && !startTime.isEmpty()) wrapper.ge(SysOperationLog::getCreateTime, startTime);
        if (endTime != null && !endTime.isEmpty()) wrapper.le(SysOperationLog::getCreateTime, endTime);
        wrapper.orderByDesc(SysOperationLog::getCreateTime);
        return page(page, wrapper);
    }
}
