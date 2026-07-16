package com.care.module.user.controller;

import com.care.common.result.Result;
import com.care.common.result.PageResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/report")
public class UserReportController {

    @GetMapping("/page")
    public Result<PageResult<Map<String, Object>>> getPage() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("id", 1);
        item.put("userName", "李明华");
        item.put("reportType", "健康报告");
        item.put("reportDate", "2026-07-01");
        item.put("status", 1);
        list.add(item);
        return Result.success(PageResult.of(1L, list));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable Long id) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("userName", "李明华");
        data.put("reportType", "健康报告");
        data.put("reportDate", "2026-07-01");
        data.put("content", "<p>健康报告内容...</p>");
        return Result.success(data);
    }
}