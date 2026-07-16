package com.care.common.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 统一 Redis 缓存读写助手。
 * - 手动使用 RedisTemplate（与现有验证码/短信码风格一致），不引入 Spring @Cacheable。
 * - 失败开放（Redis 不可用时 get 返回 null、put/evict 静默失败），不影响主流程。
 * - 热点 key 写入时加 ±30s 随机抖动，降低缓存同时失效（雪崩）风险。
 */
@Component
public class CacheHelper {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /** 随机抖动上限（秒） */
    private static final long JITTER_SECONDS = 30L;

    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Object o = get(key);
        if (o != null && type.isInstance(o)) {
            return (T) o;
        }
        return null;
    }

    public void put(String key, Object value, long ttlMinutes) {
        if (value == null) {
            return;
        }
        try {
            long jitter = Math.abs(new Random().nextLong()) % (JITTER_SECONDS + 1L);
            redisTemplate.opsForValue().set(key, value, ttlMinutes * 60L + jitter, TimeUnit.SECONDS);
        } catch (Exception ignored) {
            // 缓存写入失败不影响业务
        }
    }

    public void evict(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception ignored) {
            // 缓存删除失败不影响业务
        }
    }

    /**
     * 按前缀批量删除（仅用于低频写后失效，如管理端保存商品后清理列表缓存）。
     * 注意：prefix 视为“前缀”，内部自动补 {@code *}，因此调用方传 "product:list:" 即可，
     * 无需自行拼通配符。补 {@code *} 对已经带通配符的入参也是安全的（Redis 会将 {@code **} 等同 {@code *}）。
     */
    public void evictByPrefix(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return;
        }
        try {
            String pattern = prefix.endsWith("*") ? prefix : prefix + "*";
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception ignored) {
            // 缓存删除失败不影响业务
        }
    }
}
