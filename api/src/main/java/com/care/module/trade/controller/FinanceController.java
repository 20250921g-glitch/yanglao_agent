package com.care.module.trade.controller;

import com.care.common.result.Result;
import com.care.common.result.PageResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trade/finance")
public class FinanceController {

    @GetMapping("/page")
    public Result<PageResult<Map<String, Object>>> getPage() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("id", 1);
        item.put("orderNo", "PO20260701001");
        item.put("type", "收入");
        item.put("amount", 266.00);
        item.put("balance", 234.00);
        item.put("createTime", "2026-07-01 15:00:00");
        list.add(item);
        return Result.success(PageResult.of(1L, list));
    }
}