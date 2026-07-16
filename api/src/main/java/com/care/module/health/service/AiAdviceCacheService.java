package com.care.module.health.service;

import com.care.common.cache.CacheHelper;
import com.care.common.cache.CacheTtl;
import com.care.module.health.entity.Elder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * AI 健康建议缓存。
 * - key 结构：ai:advice:{scope}:{userId}，scope=老人ID 或 "all"（聚合全部关联老人）。
 * - 失效（evictForElder）：健康记录新增/删除后，清理该老人维度缓存（ai:advice:{elderId}:*）
 *   及所属用户的聚合缓存（ai:advice:all:{userId}），保证档案变更后建议不过期陈旧。
 * - 全程走 CacheHelper（失败开放）：Redis 不可用时 get 返回 null、put/evict 静默失败，
 *   不影响 AI 建议主流程（仅退化为每次实时调用）。
 */
@Service
public class AiAdviceCacheService {

    private static final String PREFIX = "ai:advice:";

    @Autowired
    private CacheHelper cacheHelper;
    @Autowired
    private ElderService elderService;

    /** 命中返回缓存的建议 Map（含 advice / elderCount / generatedAt），未命中返回 null。 */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getCached(Long userId, Long elderId) {
        if (userId == null) {
            return null;
        }
        return cacheHelper.get(key(userId, elderId), Map.class);
    }

    /** 写入缓存，TTL 取 CacheTtl.AI_ADVICE。 */
    public void put(Long userId, Long elderId, String advice, int elderCount) {
        if (userId == null || advice == null) {
            return;
        }
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("advice", advice);
        m.put("elderCount", elderCount);
        m.put("generatedAt", System.currentTimeMillis());
        cacheHelper.put(key(userId, elderId), m, CacheTtl.AI_ADVICE);
    }

    /**
     * 健康记录变更后使相关缓存失效。
     * @param elderId 发生变更的老人 ID
     */
    public void evictForElder(Long elderId) {
        if (elderId == null) {
            return;
        }
        // 清理该老人维度的全部用户缓存：ai:advice:{elderId}:{userId}
        cacheHelper.evictByPrefix(PREFIX + elderId + ":");
        // 清理所属用户的“全部”聚合缓存：ai:advice:all:{userId}
        Elder elder = elderService.getById(elderId);
        if (elder != null && elder.getAppUserId() != null) {
            cacheHelper.evict(PREFIX + "all:" + elder.getAppUserId());
        }
    }

    private String key(Long userId, Long elderId) {
        return PREFIX + (elderId == null ? "all" : elderId) + ":" + userId;
    }
}
