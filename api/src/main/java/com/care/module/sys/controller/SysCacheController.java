package com.care.module.sys.controller;

import com.care.common.result.Result;
import com.care.module.sys.service.CacheMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 缓存监控接口（管理员）。
 * 路径 /api/sys/cache/stats，需 JWT 认证（与后台其他 /api/sys 接口一致）。
 */
@RestController
@RequestMapping("/api/sys/cache")
public class SysCacheController {

    @Autowired
    private CacheMonitorService cacheMonitorService;

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> stats = cacheMonitorService.buildStats();
        return Result.success(stats);
    }
}
