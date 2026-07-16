package com.care.module.service.worker.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.service.worker.entity.TipRecord;
import com.care.module.service.worker.mapper.TipRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class TipRecordService extends ServiceImpl<TipRecordMapper, TipRecord> {

    public IPage<TipRecord> getPage(Integer pageNum, Integer pageSize, Long workerId,
                                    String workerName, String startDate, String endDate) {
        Page<TipRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<TipRecord> wrapper = new LambdaQueryWrapper<>();
        if (workerId != null) {
            wrapper.eq(TipRecord::getWorkerId, workerId);
        }
        if (StringUtils.hasText(workerName)) {
            wrapper.like(TipRecord::getWorkerName, workerName);
        }
        if (StringUtils.hasText(startDate)) {
            wrapper.ge(TipRecord::getCreateTime, java.time.LocalDate.parse(startDate).atStartOfDay());
        }
        if (StringUtils.hasText(endDate)) {
            wrapper.le(TipRecord::getCreateTime, java.time.LocalDate.parse(endDate).atTime(23, 59, 59));
        }
        wrapper.orderByDesc(TipRecord::getCreateTime);
        return page(page, wrapper);
    }
}
