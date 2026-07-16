package com.care.module.service.worker.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.service.worker.entity.AuditRecord;
import com.care.module.service.worker.entity.ServiceWorker;
import com.care.module.service.worker.mapper.AuditRecordMapper;
import com.care.module.service.worker.mapper.ServiceWorkerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class AuditRecordService extends ServiceImpl<AuditRecordMapper, AuditRecord> {

    @Autowired
    private ServiceWorkerMapper workerMapper;

    public IPage<AuditRecord> getPage(Integer pageNum, Integer pageSize, String workerName,
                                      Integer status, String auditType) {
        Page<AuditRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AuditRecord> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(workerName)) {
            wrapper.like(AuditRecord::getWorkerName, workerName);
        }
        if (status != null) {
            wrapper.eq(AuditRecord::getStatus, status);
        }
        if (StringUtils.hasText(auditType)) {
            wrapper.eq(AuditRecord::getAuditType, auditType);
        }
        wrapper.orderByDesc(AuditRecord::getCreateTime);
        IPage<AuditRecord> result = page(page, wrapper);
        // 填充服务人员姓名
        for (AuditRecord record : result.getRecords()) {
            ServiceWorker worker = workerMapper.selectById(record.getWorkerId());
            if (worker != null) {
                record.setWorkerName(worker.getName());
            }
        }
        return result;
    }

    public AuditRecord getByIdWithWorker(Long id) {
        AuditRecord record = getById(id);
        if (record != null) {
            ServiceWorker worker = workerMapper.selectById(record.getWorkerId());
            if (worker != null) {
                record.setWorkerName(worker.getName());
            }
        }
        return record;
    }

    public void saveRecord(Long workerId, String auditType, Integer status) {
        AuditRecord record = new AuditRecord();
        record.setWorkerId(workerId);
        record.setAuditType(auditType);
        record.setStatus(status);
        record.setAuditTime(LocalDateTime.now());
        save(record);
    }
}
