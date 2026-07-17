package com.care.module.operation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.operation.entity.DynamicComment;
import com.care.module.operation.mapper.DynamicCommentMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DynamicCommentService extends ServiceImpl<DynamicCommentMapper, DynamicComment> {

    public Map<Long, Long> countMapByDynamicIds(List<Long> dynamicIds) {
        if (dynamicIds == null || dynamicIds.isEmpty()) {
            return new HashMap<>();
        }
        LambdaQueryWrapper<DynamicComment> w = new LambdaQueryWrapper<>();
        w.in(DynamicComment::getDynamicId, dynamicIds).eq(DynamicComment::getStatus, 1);
        return list(w).stream().collect(Collectors.groupingBy(DynamicComment::getDynamicId, Collectors.counting()));
    }

    public long countByDynamicId(Long dynamicId) {
        LambdaQueryWrapper<DynamicComment> w = new LambdaQueryWrapper<>();
        w.eq(DynamicComment::getDynamicId, dynamicId).eq(DynamicComment::getStatus, 1);
        return count(w);
    }
}
