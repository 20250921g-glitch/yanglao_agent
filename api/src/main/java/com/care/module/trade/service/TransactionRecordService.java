package com.care.module.trade.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.trade.entity.TransactionRecord;
import com.care.module.trade.mapper.TransactionRecordMapper;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

@Service
public class TransactionRecordService extends ServiceImpl<TransactionRecordMapper, TransactionRecord> {

    public IPage<TransactionRecord> page(Page<TransactionRecord> page, String type,
                                         String category, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<TransactionRecord> wrapper = new LambdaQueryWrapper<>();
        if (type != null && !type.isEmpty()) {
            wrapper.eq(TransactionRecord::getType, type);
        }
        if (category != null && !category.isEmpty()) {
            wrapper.eq(TransactionRecord::getCategory, category);
        }
        if (startDate != null) {
            wrapper.ge(TransactionRecord::getCreateTime, startDate.atStartOfDay());
        }
        if (endDate != null) {
            wrapper.le(TransactionRecord::getCreateTime, endDate.plusDays(1).atStartOfDay());
        }
        wrapper.orderByDesc(TransactionRecord::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }
}
