package com.care.module.sys.controller;

import com.care.common.result.Result;
import com.care.common.result.PageResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system/sms-config")
public class SmsConfigController {

    @GetMapping("/page")
    public Result<PageResult<Map<String, Object>>> getPage() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("id", 1);
        item.put("smsType", "阿里云短信");
        item.put("accessKey", "xxx");
        item.put("accessSecret", "xxx");
        item.put("status", 1);
        list.add(item);
        return Result.success(PageResult.of(1L, list));
    }

    @PostMapping
    public Result<Void> add(@RequestBody Map<String, Object> data) {
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Map<String, Object> data) {
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return Result.success();
    }
}