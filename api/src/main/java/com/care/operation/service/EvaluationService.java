package com.care.module.operation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.operation.entity.Evaluation;
import com.care.module.operation.mapper.EvaluationMapper;
import org.springframework.stereotype.Service;

@Service
public class EvaluationService extends ServiceImpl<EvaluationMapper, Evaluation> {

    public IPage<Evaluation> getPage(Integer pageNum, Integer pageSize, String title, String elderName) {
        Page<Evaluation> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();
        if (title != null && !title.isEmpty()) {
            wrapper.like(Evaluation::getTitle, title);
        }
        if (elderName != null && !elderName.isEmpty()) {
            wrapper.like(Evaluation::getElderName, elderName);
        }
        wrapper.orderByDesc(Evaluation::getCreateTime);
        return page(page, wrapper);
    }

    public void add(Evaluation evaluation) {
        save(evaluation);
    }

    public void delete(Long id) {
        removeById(id);
    }
}
