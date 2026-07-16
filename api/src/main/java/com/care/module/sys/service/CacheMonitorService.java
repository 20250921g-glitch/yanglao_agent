package com.care.module.sys.service;

import com.care.common.cache.CacheTtl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 缓存监控：聚合 Redis 命中率与各业务分组的实际状态，支撑“命中率监控 + TTL 调优”。
 * - 全局命中率取自 Redis INFO stats（keyspace_hits / keyspace_misses），零埋点、真实反映整体缓存有效性。
 * - 各业务分组（见 {@link CacheTtl#GROUPS}）统计 key 数量、示例 key 剩余 TTL、内存占用，并对照设计 TTL。
 */
@Service
public class CacheMonitorService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public Map<String, Object> buildStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("globalHitRate", getGlobalHitRate());
        stats.put("redisMemoryUsedMb", getUsedMemoryMb());
        stats.put("totalKeys", countAllKeys());
        List<Map<String, Object>> groups = new ArrayList<>();
        for (CacheTtl.Group g : CacheTtl.GROUPS) {
            groups.add(inspectGroup(g));
        }
        stats.put("groups", groups);
        stats.put("generatedAt", new Date().toString());
        return stats;
    }

    private Map<String, Object> inspectGroup(CacheTtl.Group g) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", g.name);
        m.put("pattern", g.pattern);
        m.put("designTtlMinutes", g.designTtlMinutes);
        try {
            Set<String> keys = redisTemplate.keys(g.pattern);
            int count = (keys == null) ? 0 : keys.size();
            m.put("keyCount", count);
            if (count > 0) {
                String sample = keys.iterator().next();
                Long ttl = redisTemplate.getExpire(sample, TimeUnit.SECONDS);
                m.put("sampleKey", sample);
                m.put("sampleTtlSeconds", ttl == null ? -1L : ttl);
                Long mem = memoryUsage(sample);
                m.put("sampleMemoryBytes", mem == null ? -1L : mem);
            } else {
                m.put("sampleKey", null);
                m.put("sampleTtlSeconds", -1L);
                m.put("sampleMemoryBytes", -1L);
            }
        } catch (Exception e) {
            m.put("keyCount", -1);
            m.put("error", e.getMessage());
        }
        return m;
    }

    private Double getGlobalHitRate() {
        try {
            Properties props = redisTemplate.execute(
                    (RedisCallback<Properties>) conn -> conn.serverCommands().info());
            if (props == null) {
                return null;
            }
            Long hits = parseProp(props, "keyspace_hits");
            Long misses = parseProp(props, "keyspace_misses");
            if (hits == null || misses == null) {
                return null;
            }
            long total = hits + misses;
            if (total == 0) {
                return null;
            }
            return Math.round(hits.doubleValue() / total * 10000.0) / 100.0;
        } catch (Exception e) {
            return null;
        }
    }

    private Long getUsedMemoryMb() {
        try {
            Properties props = redisTemplate.execute(
                    (RedisCallback<Properties>) conn -> conn.serverCommands().info("memory"));
            if (props == null) {
                return null;
            }
            Long bytes = parseProp(props, "used_memory");
            return bytes == null ? null : bytes / (1024 * 1024);
        } catch (Exception e) {
            return null;
        }
    }

    private Long parseProp(Properties props, String key) {
        String v = props.getProperty(key);
        if (v == null) {
            return null;
        }
        try {
            return Long.parseLong(v.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int countAllKeys() {
        try {
            Set<String> keys = redisTemplate.keys("*");
            return keys == null ? 0 : keys.size();
        } catch (Exception e) {
            return -1;
        }
    }

    private Long memoryUsage(String key) {
        try {
            Object o = redisTemplate.execute((RedisCallback<Object>) conn ->
                    conn.execute("MEMORY",
                            "USAGE".getBytes(StandardCharsets.UTF_8),
                            key.getBytes(StandardCharsets.UTF_8)));
            if (o == null) {
                return null;
            }
            if (o instanceof Long) {
                return (Long) o;
            }
            if (o instanceof Integer) {
                return ((Integer) o).longValue();
            }
            if (o instanceof byte[]) {
                return Long.parseLong(new String((byte[]) o, StandardCharsets.UTF_8).trim());
            }
            try {
                return Long.parseLong(o.toString().trim());
            } catch (Exception ignore) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
}
