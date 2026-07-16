package com.care.module.operation.controller;

import com.care.common.result.Result;
import com.care.common.result.PageResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/operation/report")
public class ReportController {

    @GetMapping("/page")
    public Result<PageResult<Map<String, Object>>> getPage() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("id", 1);
        item.put("reportType", "内容举报");
        item.put("targetId", 1);
        item.put("targetType", "dynamic");
        item.put("reason", "内容违规");
        item.put("description", "该动态包含不当内容");
        item.put("reporter", "李明华");
        item.put("status", 0);
        item.put("createTime", "2026-07-01 10:00:00");
        list.add(item);
        return Result.success(PageResult.of(1L, list));
    }

    @PutMapping("/{id}/handle")
    public Result<Void> handle(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        return Result.success();
    }
}