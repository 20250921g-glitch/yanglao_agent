package com.care.module.operation.controller;

import com.care.common.result.Result;
import com.care.common.result.PageResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/operation/medicine")
public class MedicineController {

    @GetMapping("/page")
    public Result<PageResult<Map<String, Object>>> getPage() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("id", 1);
        item.put("name", "阿司匹林肠溶片");
        item.put("category", "心血管");
        item.put("dosage", "每日1片");
        item.put("indication", "用于预防心肌梗死、脑梗死");
        item.put("contraindication", "对阿司匹林过敏者禁用");
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