package com.care.module.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.user.entity.GrowthRule;
import com.care.module.user.mapper.GrowthRuleMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class GrowthRuleService extends ServiceImpl<GrowthRuleMapper, GrowthRule> {

    public IPage<GrowthRule> getPage(Integer pageNum, Integer pageSize, String keyword) {
        Page<GrowthRule> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<GrowthRule> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(GrowthRule::getRuleName, keyword);
        }
        wrapper.orderByDesc(GrowthRule::getCreateTime);
        return page(page, wrapper);
    }

    public void saveRule(GrowthRule rule) {
        if (rule.getId() == null) {
            if (!StringUtils.hasText(rule.getRuleCode())) {
                rule.setRuleCode("RULE_" + System.currentTimeMillis());
            }
            if (!StringUtils.hasText(rule.getActionType())) {
                rule.setActionType("DEFAULT");
            }
            save(rule);
        } else {
            updateById(rule);
        }
    }

    public void updateStatus(Long id, Integer status) {
        GrowthRule rule = getById(id);
        if (rule == null) throw new RuntimeException("规则不存在");
        rule.setStatus(status);
        updateById(rule);
    }

    public void deleteRule(Long id) {
        removeById(id);
    }
}
