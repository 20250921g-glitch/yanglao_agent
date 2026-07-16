package com.care.module.operation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.common.cache.CacheHelper;
import com.care.common.cache.CacheTtl;
import com.care.module.operation.entity.Dynamic;
import com.care.module.operation.entity.DynamicCount;
import com.care.module.operation.mapper.DynamicMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class DynamicService extends ServiceImpl<DynamicMapper, Dynamic> {

    @Autowired
    private DynamicLikeService likeService;
    @Autowired
    private DynamicFavoriteService favoriteService;
    @Autowired
    private DynamicCommentService commentService;
    @Autowired
    private CacheHelper cacheHelper;

    /** 动态计数缓存 key 前缀（方式A：以关系表为准实时聚合后缓存，TTL 1 分钟，写后失效） */
    private static final String COUNT_KEY = "dynamic:count:";

    public IPage<Dynamic> getPage(Integer pageNum, Integer pageSize, Integer status, Long userId,
                                  String startCreateTime, String endCreateTime) {
        Page<Dynamic> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Dynamic> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Dynamic::getStatus, status);
        }
        if (userId != null) {
            wrapper.eq(Dynamic::getUserId, userId);
        }
        if (startCreateTime != null && !startCreateTime.isEmpty()) {
            wrapper.ge(Dynamic::getCreateTime, startCreateTime + " 00:00:00");
        }
        if (endCreateTime != null && !endCreateTime.isEmpty()) {
            wrapper.le(Dynamic::getCreateTime, endCreateTime + " 23:59:59");
        }
        wrapper.orderByDesc(Dynamic::getCreateTime);
        IPage<Dynamic> result = page(page, wrapper);
        result.getRecords().forEach(this::fillStatusText);
        return result;
    }

    /** 单条动态聚合计数（带缓存） */
    public DynamicCount getCounts(Long dynamicId) {
        DynamicCount cached = cacheHelper.get(COUNT_KEY + dynamicId, DynamicCount.class);
        if (cached != null) {
            return cached;
        }
        DynamicCount c = computeCounts(dynamicId);
        cacheHelper.put(COUNT_KEY + dynamicId, c, CacheTtl.NEIGHBOR_COUNT);
        return c;
    }

    /** 批量聚合计数（带缓存 + 仅对未命中做批量查询，消除 N+1） */
    public Map<Long, DynamicCount> getCountsBatch(List<Long> dynamicIds) {
        Map<Long, DynamicCount> result = new HashMap<>();
        if (dynamicIds == null || dynamicIds.isEmpty()) {
            return result;
        }
        List<Long> miss = new ArrayList<>();
        for (Long id : dynamicIds) {
            DynamicCount c = cacheHelper.get(COUNT_KEY + id, DynamicCount.class);
            if (c != null) {
                result.put(id, c);
            } else {
                miss.add(id);
            }
        }
        if (!miss.isEmpty()) {
            Map<Long, Long> likeMap = likeService.countMapByDynamicIds(miss);
            Map<Long, Long> favMap = favoriteService.countMapByDynamicIds(miss);
            Map<Long, Long> cmtMap = commentService.countMapByDynamicIds(miss);
            Map<Long, Integer> shareMap = new HashMap<>();
            List<Dynamic> dynamics = listByIds(miss);
            for (Dynamic d : dynamics) {
                shareMap.put(d.getId(), d.getShareCount() == null ? 0 : d.getShareCount());
            }
            for (Long id : miss) {
                DynamicCount c = new DynamicCount(
                        toInt(likeMap.get(id)),
                        toInt(favMap.get(id)),
                        shareMap.getOrDefault(id, 0),
                        toInt(cmtMap.get(id)));
                cacheHelper.put(COUNT_KEY + id, c, CacheTtl.NEIGHBOR_COUNT);
                result.put(id, c);
            }
        }
        return result;
    }

    /** 写操作后失效单条计数缓存 */
    public void evictCounts(Long dynamicId) {
        if (dynamicId != null) {
            cacheHelper.evict(COUNT_KEY + dynamicId);
        }
    }

    private DynamicCount computeCounts(Long dynamicId) {
        long like = likeService.countByDynamicId(dynamicId);
        long fav = favoriteService.countByDynamicId(dynamicId);
        long cmt = commentService.countByDynamicId(dynamicId);
        Dynamic d = getById(dynamicId);
        int share = (d != null && d.getShareCount() != null) ? d.getShareCount() : 0;
        return new DynamicCount((int) like, (int) fav, share, (int) cmt);
    }

    private int toInt(Long v) {
        return v == null ? 0 : v.intValue();
    }

    public boolean audit(Long id, Integer status, String remark) {
        Dynamic dynamic = getById(id);
        if (dynamic == null || dynamic.getStatus() != 0) {
            return false;
        }
        dynamic.setStatus(status);
        dynamic.setAuditRemark(remark);
        updateById(dynamic);
        return true;
    }

    public boolean publish(Dynamic dynamic) {
        if (dynamic.getStatus() == null) {
            dynamic.setStatus(0);
        }
        if (dynamic.getViewCount() == null) {
            dynamic.setViewCount(0);
        }
        if (dynamic.getLikeCount() == null) {
            dynamic.setLikeCount(0);
        }
        return save(dynamic);
    }

    public void fillStatusText(Dynamic dynamic) {
        switch (dynamic.getStatus()) {
            case 0: dynamic.setStatusText("待审核"); break;
            case 1: dynamic.setStatusText("已通过"); break;
            case 2: dynamic.setStatusText("已拒绝"); break;
            case 3: dynamic.setStatusText("已下架"); break;
            default: dynamic.setStatusText("未知");
        }
    }
}