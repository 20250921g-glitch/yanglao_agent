package com.care.module.trade.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.trade.entity.Withdrawal;
import com.care.module.trade.mapper.WithdrawalMapper;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class WithdrawalService extends ServiceImpl<WithdrawalMapper, Withdrawal> {

    public IPage<Withdrawal> page(Page<Withdrawal> page, String workerName, Integer status) {
        LambdaQueryWrapper<Withdrawal> wrapper = new LambdaQueryWrapper<>();
        if (workerName != null && !workerName.isEmpty()) {
            wrapper.like(Withdrawal::getWorkerName, workerName);
        }
        if (status != null) {
            wrapper.eq(Withdrawal::getStatus, status);
        }
        wrapper.orderByDesc(Withdrawal::getCreateTime);
        IPage<Withdrawal> result = baseMapper.selectPage(page, wrapper);
        result.getRecords().forEach(this::fillStatusText);
        return result;
    }

    public Withdrawal getById(Long id) {
        Withdrawal w = baseMapper.selectById(id);
        if (w != null) {
            fillStatusText(w);
        }
        return w;
    }

    public boolean audit(Long id, Integer status, String rejectReason,
                         Long auditorId, String auditorName) {
        Withdrawal w = baseMapper.selectById(id);
        if (w == null) {
            return false;
        }
        w.setStatus(status);
        w.setAuditTime(LocalDateTime.now());
        w.setAuditorId(auditorId);
        w.setAuditorName(auditorName);
        if (status == 2) {
            w.setRejectReason(rejectReason);
        }
        if (status == 1) {
            w.setCompleteTime(LocalDateTime.now());
        }
        baseMapper.updateById(w);
        return true;
    }

    private void fillStatusText(Withdrawal w) {
        switch (w.getStatus()) {
            case 0: w.setStatusText("待审核"); break;
            case 1: w.setStatusText("已通过"); break;
            case 2: w.setStatusText("已拒绝"); break;
            default: w.setStatusText("未知");
        }
    }
}
