package com.care.module.trade.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.trade.entity.Refund;
import com.care.module.trade.mapper.RefundMapper;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class RefundService extends ServiceImpl<RefundMapper, Refund> {

    public IPage<Refund> page(Page<Refund> page, String refundNo, Integer status,
                              LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<Refund> wrapper = new LambdaQueryWrapper<>();
        if (refundNo != null && !refundNo.isEmpty()) {
            wrapper.like(Refund::getRefundNo, refundNo);
        }
        if (status != null) {
            wrapper.eq(Refund::getStatus, status);
        }
        if (startDate != null) {
            wrapper.ge(Refund::getCreateTime, startDate.atStartOfDay());
        }
        if (endDate != null) {
            wrapper.le(Refund::getCreateTime, endDate.plusDays(1).atStartOfDay());
        }
        wrapper.orderByDesc(Refund::getCreateTime);
        IPage<Refund> result = baseMapper.selectPage(page, wrapper);
        result.getRecords().forEach(this::fillStatusText);
        return result;
    }

    public Refund getById(Long id) {
        Refund refund = baseMapper.selectById(id);
        if (refund != null) {
            fillStatusText(refund);
        }
        return refund;
    }

    public Refund saveRefund(Refund refund) {
        refund.setRefundNo("RF" + System.currentTimeMillis());
        refund.setStatus(0);
        refund.setCreateTime(LocalDateTime.now());
        baseMapper.insert(refund);
        return refund;
    }

    public boolean approve(Long id, String auditor, String remark) {
        Refund refund = baseMapper.selectById(id);
        if (refund == null || refund.getStatus() != 0) {
            return false;
        }
        refund.setStatus(1);
        refund.setHandlerName(auditor);
        refund.setHandleRemark(remark);
        refund.setHandleTime(LocalDateTime.now());
        baseMapper.updateById(refund);
        return true;
    }

    public boolean reject(Long id, String auditor, String remark) {
        Refund refund = baseMapper.selectById(id);
        if (refund == null || refund.getStatus() != 0) {
            return false;
        }
        refund.setStatus(2);
        refund.setHandlerName(auditor);
        refund.setHandleRemark(remark);
        refund.setHandleTime(LocalDateTime.now());
        baseMapper.updateById(refund);
        return true;
    }

    public boolean processRefund(Long id) {
        Refund refund = baseMapper.selectById(id);
        if (refund == null || refund.getStatus() != 1) {
            return false;
        }
        refund.setStatus(3);
        refund.setHandleTime(LocalDateTime.now());
        baseMapper.updateById(refund);
        return true;
    }

    public boolean cancel(Long id) {
        Refund refund = baseMapper.selectById(id);
        if (refund == null || refund.getStatus() >= 3) {
            return false;
        }
        refund.setStatus(4);
        refund.setHandleTime(LocalDateTime.now());
        baseMapper.updateById(refund);
        return true;
    }

    public boolean audit(Long id, Integer status, BigDecimal actualAmount,
                         String handleRemark, Long handlerId, String handlerName) {
        Refund refund = baseMapper.selectById(id);
        if (refund == null) {
            return false;
        }
        refund.setStatus(status);
        refund.setActualAmount(actualAmount);
        refund.setHandleRemark(handleRemark);
        refund.setHandlerId(handlerId);
        refund.setHandlerName(handlerName);
        refund.setHandleTime(LocalDateTime.now());
        baseMapper.updateById(refund);
        return true;
    }

    private void fillStatusText(Refund refund) {
        switch (refund.getStatus()) {
            case 0: refund.setStatusText("待审核"); break;
            case 1: refund.setStatusText("审核通过"); break;
            case 2: refund.setStatusText("审核拒绝"); break;
            case 3: refund.setStatusText("已退款"); break;
            case 4: refund.setStatusText("已取消"); break;
            default: refund.setStatusText("未知");
        }
    }
}
