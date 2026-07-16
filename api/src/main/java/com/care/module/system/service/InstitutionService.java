package com.care.module.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.system.entity.Institution;
import com.care.module.system.mapper.InstitutionMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InstitutionService extends ServiceImpl<InstitutionMapper, Institution> {

    public IPage<Institution> getPage(Integer pageNum, Integer pageSize, String type, Integer status) {
        Page<Institution> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Institution> wrapper = new LambdaQueryWrapper<>();
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Institution::getType, type);
        }
        if (status != null) {
            wrapper.eq(Institution::getStatus, status);
        }
        wrapper.orderByDesc(Institution::getCreateTime);
        IPage<Institution> result = page(page, wrapper);
        result.getRecords().forEach(this::fillText);
        return result;
    }

    public List<Institution> getAll(Integer status) {
        LambdaQueryWrapper<Institution> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Institution::getStatus, status);
        }
        wrapper.orderByDesc(Institution::getCreateTime);
        List<Institution> list = list(wrapper);
        list.forEach(this::fillText);
        return list;
    }

    public void fillText(Institution institution) {
        Map<String, String> typeMap = new HashMap<>();
        typeMap.put("nursing_home", "养老院");
        typeMap.put("home_care", "居家护理");
        typeMap.put("day_care", "日间照料");
        typeMap.put("medical", "医疗机构");
        institution.setTypeText(typeMap.getOrDefault(institution.getType(), institution.getType()));

        if (institution.getStatus() == 1) {
            institution.setStatusText("启用");
        } else {
            institution.setStatusText("禁用");
        }
    }
}