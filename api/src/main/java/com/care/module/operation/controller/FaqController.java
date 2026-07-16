package com.care.module.operation.controller;

import com.care.common.result.Result;
import com.care.common.result.PageResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/operation/faq")
public class FaqController {

    @GetMapping("/page")
    public Result<PageResult<Map<String, Object>>> getPage() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("id", 1);
        item.put("question", "如何预约服务？");
        item.put("answer", "登录APP后，选择服务类型，填写预约时间即可");
        item.put("sort", 1);
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