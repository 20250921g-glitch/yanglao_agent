package com.care.module.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.user.entity.PointsRule;
import com.care.module.user.mapper.PointsRuleMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PointsRuleService extends ServiceImpl<PointsRuleMapper, PointsRule> {

    public IPage<PointsRule> getPage(Integer pageNum, Integer pageSize, String ruleName, String actionType) {
        Page<PointsRule> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PointsRule> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(ruleName)) w.like(PointsRule::getRuleName, ruleName);
        if (StringUtils.hasText(actionType)) w.eq(PointsRule::getActionType, actionType);
        w.orderByDesc(PointsRule::getCreateTime);
        return page(page, w);
    }
}
