package com.care.module.operation.controller;

import com.care.common.result.Result;
import com.care.common.result.PageResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/operation/opinion")
public class OpinionController {

    @GetMapping("/page")
    public Result<PageResult<Map<String, Object>>> getPage() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("id", 1);
        item.put("userName", "李明华");
        item.put("content", "建议增加更多康复训练视频");
        item.put("status", 0);
        item.put("reply", "");
        item.put("createTime", "2026-07-01 10:00:00");
        list.add(item);
        return Result.success(PageResult.of(1L, list));
    }

    @PutMapping("/{id}/reply")
    public Result<Void> reply(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        return Result.success();
    }
}