package com.care.module.health.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.health.entity.HealthRecord;
import com.care.module.health.mapper.HealthRecordMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HealthRecordService extends ServiceImpl<HealthRecordMapper, HealthRecord> {

    public IPage<HealthRecord> getPage(Integer pageNum, Integer pageSize, Long elderId, String recordType,
                                        LocalDate startDate, LocalDate endDate) {
        Page<HealthRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<HealthRecord> wrapper = new LambdaQueryWrapper<>();
        if (elderId != null) {
            wrapper.eq(HealthRecord::getElderId, elderId);
        }
        if (recordType != null && !recordType.isEmpty()) {
            wrapper.eq(HealthRecord::getRecordType, recordType);
        }
        if (startDate != null) {
            wrapper.ge(HealthRecord::getRecordTime, startDate.atStartOfDay());
        }
        if (endDate != null) {
            wrapper.le(HealthRecord::getRecordTime, endDate.plusDays(1).atStartOfDay());
        }
        wrapper.orderByDesc(HealthRecord::getRecordTime);
        return page(page, wrapper);
    }

    public List<HealthRecord> getByElderId(Long elderId) {
        return list(new LambdaQueryWrapper<HealthRecord>()
                .eq(HealthRecord::getElderId, elderId)
                .orderByDesc(HealthRecord::getRecordTime));
    }

    public List<HealthRecord> getByType(Long elderId, String recordType) {
        LambdaQueryWrapper<HealthRecord> wrapper = new LambdaQueryWrapper<>();
        if (elderId != null) {
            wrapper.eq(HealthRecord::getElderId, elderId);
        }
        if (recordType != null && !recordType.isEmpty()) {
            wrapper.eq(HealthRecord::getRecordType, recordType);
        }
        wrapper.orderByDesc(HealthRecord::getRecordTime);
        return list(wrapper);
    }

    public Map<String, Object> getStats(Long elderId, String recordType) {
        Map<String, Object> stats = new HashMap<>();
        List<HealthRecord> records = getByType(elderId, recordType);
        
        if (records.isEmpty()) {
            stats.put("latestValue", "-");
            stats.put("avgValue", "-");
            stats.put("maxValue", "-");
            stats.put("minValue", "-");
            stats.put("count", 0);
            return stats;
        }

        List<Double> values = new ArrayList<>();
        for (HealthRecord record : records) {
            try {
                values.add(Double.parseDouble(record.getRecordValue()));
            } catch (Exception e) {}
        }

        stats.put("latestValue", records.get(0).getRecordValue());
        stats.put("count", records.size());

        if (!values.isEmpty()) {
            double sum = values.stream().mapToDouble(Double::doubleValue).sum();
            stats.put("avgValue", String.format("%.1f", sum / values.size()));
            stats.put("maxValue", String.format("%.1f", values.stream().mapToDouble(Double::doubleValue).max().orElse(0)));
            stats.put("minValue", String.format("%.1f", values.stream().mapToDouble(Double::doubleValue).min().orElse(0)));
        } else {
            stats.put("avgValue", "-");
            stats.put("maxValue", "-");
            stats.put("minValue", "-");
        }

        return stats;
    }

    public void add(HealthRecord record) {
        if (record.getRecordTime() == null) {
            record.setRecordTime(LocalDateTime.now());
        }
        save(record);
    }

    public void delete(Long id) {
        removeById(id);
    }
}
