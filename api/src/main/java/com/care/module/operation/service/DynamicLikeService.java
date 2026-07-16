package com.care.module.operation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.operation.entity.DynamicLike;
import com.care.module.operation.mapper.DynamicLikeMapper;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DynamicLikeService extends ServiceImpl<DynamicLikeMapper, DynamicLike> {

    public boolean liked(Long dynamicId, Long userId) {
        return lambdaQuery()
                .eq(DynamicLike::getDynamicId, dynamicId)
                .eq(DynamicLike::getUserId, userId)
                .count() > 0;
    }

    /** 批量判断：给定动态ID集合，返回当前用户点赞过的动态ID集合（一次 IN 查询，消除 N+1） */
    public Set<Long> likedIds(Long userId, List<Long> dynamicIds) {
        if (dynamicIds == null || dynamicIds.isEmpty()) {
            return new HashSet<>();
        }
        LambdaQueryWrapper<DynamicLike> w = new LambdaQueryWrapper<>();
        w.in(DynamicLike::getDynamicId, dynamicIds).eq(DynamicLike::getUserId, userId);
        return list(w).stream().map(DynamicLike::getDynamicId).collect(Collectors.toSet());
    }

    public long countByDynamicId(Long dynamicId) {
        LambdaQueryWrapper<DynamicLike> w = new LambdaQueryWrapper<>();
        w.eq(DynamicLike::getDynamicId, dynamicId);
        return count(w);
    }

    public Map<Long, Long> countMapByDynamicIds(List<Long> dynamicIds) {
        if (dynamicIds == null || dynamicIds.isEmpty()) {
            return new java.util.HashMap<>();
        }
        LambdaQueryWrapper<DynamicLike> w = new LambdaQueryWrapper<>();
        w.in(DynamicLike::getDynamicId, dynamicIds);
        return list(w).stream().collect(Collectors.groupingBy(DynamicLike::getDynamicId, Collectors.counting()));
    }
}
