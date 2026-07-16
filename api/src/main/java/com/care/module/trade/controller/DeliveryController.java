package com.care.module.trade.controller;

import com.care.common.result.Result;
import com.care.common.result.PageResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trade/delivery")
public class DeliveryController {

    @GetMapping("/page")
    public Result<PageResult<Map<String, Object>>> getPage() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("id", 1);
        item.put("orderNo", "PO20260701001");
        item.put("elderName", "李明华");
        item.put("address", "北京市朝阳区幸福小区3号楼201室");
        item.put("deliveryType", "快递");
        item.put("trackingNo", "SF1234567890");
        item.put("status", 3);
        item.put("createTime", "2026-07-01 15:00:00");
        list.add(item);
        return Result.success(PageResult.of(1L, list));
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return Result.success();
    }
}