package com.care.module.sys.controller;

import com.care.common.result.Result;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/system/about")
public class AboutController {

    @GetMapping
    public Result<Map<String, Object>> getAbout() {
        Map<String, Object> data = new HashMap<>();
        data.put("systemName", "智慧养老后台管理系统");
        data.put("version", "1.0.0");
        data.put("description", "智能健康信息及服务管理；实现资源的优化配置和管理，降低运营成本");
        data.put("copyright", "2026 智慧养老系统");
        return Result.success(data);
    }
}