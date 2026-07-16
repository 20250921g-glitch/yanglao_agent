package com.care.module.trade.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.trade.entity.RefundReason;
import com.care.module.trade.mapper.RefundReasonMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RefundReasonService extends ServiceImpl<RefundReasonMapper, RefundReason> {

    public List<RefundReason> listEnabled() {
        LambdaQueryWrapper<RefundReason> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RefundReason::getStatus, 1).orderByAsc(RefundReason::getSort);
        return baseMapper.selectList(wrapper);
    }

    public RefundReason getById(Long id) {
        return baseMapper.selectById(id);
    }

    public boolean saveReason(RefundReason reason) {
        return baseMapper.insert(reason) > 0;
    }

    public boolean updateReason(RefundReason reason) {
        return baseMapper.updateById(reason) > 0;
    }

    public boolean deleteById(Long id) {
        return baseMapper.deleteById(id) > 0;
    }
}
