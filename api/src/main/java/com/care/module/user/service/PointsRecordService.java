package com.care.module.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.user.entity.AppUser;
import com.care.module.user.entity.PointsRecord;
import com.care.module.user.mapper.AppUserMapper;
import com.care.module.user.mapper.PointsRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PointsRecordService extends ServiceImpl<PointsRecordMapper, PointsRecord> {

    @Autowired
    private AppUserMapper appUserMapper;

    public IPage<PointsRecord> getPage(Integer pageNum, Integer pageSize, Long userId, String type, String source) {
        Page<PointsRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PointsRecord> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(PointsRecord::getUserId, userId);
        }
        if (type != null && !type.isEmpty()) {
            wrapper.eq(PointsRecord::getType, type);
        }
        if (source != null && !source.isEmpty()) {
            wrapper.eq(PointsRecord::getSource, source);
        }
        wrapper.orderByDesc(PointsRecord::getCreateTime);
        IPage<PointsRecord> result = page(page, wrapper);
        result.getRecords().forEach(this::fillText);
        return result;
    }

    public Map<String, Object> getStats(Long userId, String type) {
        Map<String, Object> stats = new HashMap<>();
        
        LambdaQueryWrapper<PointsRecord> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(PointsRecord::getUserId, userId);
        }
        if (type != null && !type.isEmpty()) {
            wrapper.eq(PointsRecord::getType, type);
        }
        
        List<PointsRecord> records = list(wrapper);
        
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        BigDecimal currentBalance = BigDecimal.ZERO;
        
        for (PointsRecord record : records) {
            if (record.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                totalIncome = totalIncome.add(record.getAmount());
            } else {
                totalExpense = totalExpense.add(record.getAmount().abs());
            }
            if (record.getBalance().compareTo(currentBalance) > 0) {
                currentBalance = record.getBalance();
            }
        }
        
        stats.put("totalIncome", totalIncome);
        stats.put("totalExpense", totalExpense);
        stats.put("currentBalance", currentBalance);
        stats.put("recordCount", records.size());
        
        return stats;
    }

    public boolean adjustPoints(Long userId, String type, BigDecimal amount, String source, String remark) {
        AppUser user = appUserMapper.selectById(userId);
        if (user == null) {
            return false;
        }

        BigDecimal currentPoints = user.getPoints() != null ? user.getPoints() : BigDecimal.ZERO;
        BigDecimal newPoints = currentPoints.add(amount);
        
        if (newPoints.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }

        user.setPoints(newPoints);
        appUserMapper.updateById(user);

        PointsRecord record = new PointsRecord();
        record.setUserId(userId);
        record.setUserName(user.getRealName());
        record.setType(type);
        record.setSource(source);
        record.setAmount(amount);
        record.setBalance(newPoints);
        record.setRemark(remark);
        record.setCreateTime(LocalDateTime.now());
        baseMapper.insert(record);

        return true;
    }

    private void fillText(PointsRecord record) {
        if ("points".equals(record.getType())) {
            record.setTypeText("积分");
        } else if ("growth".equals(record.getType())) {
            record.setTypeText("成长值");
        } else {
            record.setTypeText(record.getType());
        }
        
        Map<String, String> sourceMap = new HashMap<>();
        sourceMap.put("register", "注册");
        sourceMap.put("sign", "签到");
        sourceMap.put("order", "订单完成");
        sourceMap.put("comment", "评价");
        sourceMap.put("exchange", "兑换");
        sourceMap.put("adjust", "管理员调整");
        record.setSourceText(sourceMap.getOrDefault(record.getSource(), record.getSource()));
    }
}