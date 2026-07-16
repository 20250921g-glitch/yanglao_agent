package com.care.module.trade.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.trade.entity.OrderEvaluation;
import com.care.module.trade.mapper.OrderEvaluationMapper;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class OrderEvaluationService extends ServiceImpl<OrderEvaluationMapper, OrderEvaluation> {

    public IPage<OrderEvaluation> page(Page<OrderEvaluation> page, String orderNo,
                                       Integer score, Integer status) {
        LambdaQueryWrapper<OrderEvaluation> wrapper = new LambdaQueryWrapper<>();
        if (orderNo != null && !orderNo.isEmpty()) {
            wrapper.like(OrderEvaluation::getOrderNo, orderNo);
        }
        if (score != null) {
            wrapper.eq(OrderEvaluation::getScore, score);
        }
        if (status != null) {
            wrapper.eq(OrderEvaluation::getStatus, status);
        }
        wrapper.orderByDesc(OrderEvaluation::getCreateTime);
        IPage<OrderEvaluation> result = baseMapper.selectPage(page, wrapper);
        result.getRecords().forEach(this::fillStatusText);
        return result;
    }

    public OrderEvaluation getById(Long id) {
        OrderEvaluation eval = baseMapper.selectById(id);
        if (eval != null) {
            fillStatusText(eval);
        }
        return eval;
    }

    public boolean saveEval(OrderEvaluation evaluation) {
        return baseMapper.insert(evaluation) > 0;
    }

    public boolean updateEval(OrderEvaluation evaluation) {
        return baseMapper.updateById(evaluation) > 0;
    }

    public boolean deleteById(Long id) {
        return baseMapper.deleteById(id) > 0;
    }

    public boolean reply(Long id, String reply) {
        OrderEvaluation eval = baseMapper.selectById(id);
        if (eval == null) {
            return false;
        }
        eval.setReply(reply);
        eval.setReplyTime(LocalDateTime.now());
        baseMapper.updateById(eval);
        return true;
    }

    public boolean updateStatus(Long id, Integer status) {
        OrderEvaluation eval = baseMapper.selectById(id);
        if (eval == null) {
            return false;
        }
        eval.setStatus(status);
        baseMapper.updateById(eval);
        return true;
    }

    private void fillStatusText(OrderEvaluation eval) {
        eval.setStatusText("1".equals(String.valueOf(eval.getStatus())) ? "显示" : "隐藏");
    }
}
