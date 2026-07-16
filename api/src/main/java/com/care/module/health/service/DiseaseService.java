package com.care.module.health.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.health.entity.Disease;
import com.care.module.health.mapper.DiseaseMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DiseaseService extends ServiceImpl<DiseaseMapper, Disease> {

    public IPage<Disease> getPage(Integer pageNum, Integer pageSize, String category, Integer status) {
        Page<Disease> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Disease> wrapper = new LambdaQueryWrapper<>();
        if (category != null && !category.isEmpty()) {
            wrapper.eq(Disease::getCategory, category);
        }
        if (status != null) {
            wrapper.eq(Disease::getStatus, status);
        }
        wrapper.orderByDesc(Disease::getCreateTime);
        IPage<Disease> result = page(page, wrapper);
        result.getRecords().forEach(this::fillText);
        return result;
    }

    public List<Disease> getAll(Integer status) {
        LambdaQueryWrapper<Disease> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Disease::getStatus, status);
        }
        wrapper.orderByDesc(Disease::getCreateTime);
        List<Disease> list = list(wrapper);
        list.forEach(this::fillText);
        return list;
    }

    public void fillText(Disease disease) {
        Map<String, String> categoryMap = new HashMap<>();
        categoryMap.put("cardiovascular", "心血管疾病");
        categoryMap.put("diabetes", "糖尿病");
        categoryMap.put("respiratory", "呼吸系统疾病");
        categoryMap.put("neurological", "神经系统疾病");
        categoryMap.put("musculoskeletal", "骨关节疾病");
        disease.setCategoryText(categoryMap.getOrDefault(disease.getCategory(), disease.getCategory()));

        if (disease.getStatus() == 1) {
            disease.setStatusText("启用");
        } else {
            disease.setStatusText("禁用");
        }
    }
}