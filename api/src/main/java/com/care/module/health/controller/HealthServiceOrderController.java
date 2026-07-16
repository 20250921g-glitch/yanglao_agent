package com.care.module.health.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.health.entity.HealthServiceOrder;
import com.care.module.health.service.HealthServiceOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "健康管理-健康服务订单")
@RestController
@RequestMapping("/api/health/service-order")
public class HealthServiceOrderController {

    @Autowired
    private HealthServiceOrderService healthServiceOrderService;

    @ApiOperation("分页查询健康服务订单")
    @GetMapping("/page")
    public Result<PageResult<HealthServiceOrder>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long elderId,
            @RequestParam(required = false) Integer status) {
        IPage<HealthServiceOrder> result = healthServiceOrderService.getPage(pageNum, pageSize, elderId, status);
        return Result.success(PageResult.of(result));
    }

    @ApiOperation("根据ID查询健康服务订单")
    @GetMapping("/{id}")
    public Result<HealthServiceOrder> getById(@PathVariable Long id) {
        return Result.success(healthServiceOrderService.getById(id));
    }
}
