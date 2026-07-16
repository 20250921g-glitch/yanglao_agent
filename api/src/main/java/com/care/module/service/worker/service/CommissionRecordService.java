package com.care.module.service.worker.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.trade.entity.CommissionRecord;
import com.care.module.trade.mapper.CommissionRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CommissionRecordService extends ServiceImpl<CommissionRecordMapper, CommissionRecord> {

    public IPage<CommissionRecord> getPage(Integer pageNum, Integer pageSize, Long workerId,
                                            String workerName, Integer status, String startDate, String endDate) {
        Page<CommissionRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CommissionRecord> wrapper = new LambdaQueryWrapper<>();
        if (workerId != null) {
            wrapper.eq(CommissionRecord::getWorkerId, workerId);
        }
        if (StringUtils.hasText(workerName)) {
            wrapper.like(CommissionRecord::getWorkerName, workerName);
        }
        if (status != null) {
            wrapper.eq(CommissionRecord::getStatus, status);
        }
        if (StringUtils.hasText(startDate)) {
            wrapper.ge(CommissionRecord::getCreateTime, java.time.LocalDate.parse(startDate).atStartOfDay());
        }
        if (StringUtils.hasText(endDate)) {
            wrapper.le(CommissionRecord::getCreateTime, java.time.LocalDate.parse(endDate).atTime(23, 59, 59));
        }
        wrapper.orderByDesc(CommissionRecord::getCreateTime);
        return page(page, wrapper);
    }
}
