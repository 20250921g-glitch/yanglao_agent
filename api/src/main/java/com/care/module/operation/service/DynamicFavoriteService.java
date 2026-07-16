package com.care.module.operation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.operation.entity.DynamicFavorite;
import com.care.module.operation.mapper.DynamicFavoriteMapper;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DynamicFavoriteService extends ServiceImpl<DynamicFavoriteMapper, DynamicFavorite> {

    public boolean favorited(Long dynamicId, Long userId) {
        return lambdaQuery()
                .eq(DynamicFavorite::getDynamicId, dynamicId)
                .eq(DynamicFavorite::getUserId, userId)
                .count() > 0;
    }

    /** 批量判断：给定动态ID集合，返回当前用户收藏过的动态ID集合（一次 IN 查询，消除 N+1） */
    public Set<Long> favoritedIds(Long userId, List<Long> dynamicIds) {
        if (dynamicIds == null || dynamicIds.isEmpty()) {
            return new HashSet<>();
        }
        LambdaQueryWrapper<DynamicFavorite> w = new LambdaQueryWrapper<>();
        w.in(DynamicFavorite::getDynamicId, dynamicIds).eq(DynamicFavorite::getUserId, userId);
        return list(w).stream().map(DynamicFavorite::getDynamicId).collect(Collectors.toSet());
    }

    public long countByDynamicId(Long dynamicId) {
        LambdaQueryWrapper<DynamicFavorite> w = new LambdaQueryWrapper<>();
        w.eq(DynamicFavorite::getDynamicId, dynamicId);
        return count(w);
    }

    public Map<Long, Long> countMapByDynamicIds(List<Long> dynamicIds) {
        if (dynamicIds == null || dynamicIds.isEmpty()) {
            return new java.util.HashMap<>();
        }
        LambdaQueryWrapper<DynamicFavorite> w = new LambdaQueryWrapper<>();
        w.in(DynamicFavorite::getDynamicId, dynamicIds);
        return list(w).stream().collect(Collectors.groupingBy(DynamicFavorite::getDynamicId, Collectors.counting()));
    }
}
