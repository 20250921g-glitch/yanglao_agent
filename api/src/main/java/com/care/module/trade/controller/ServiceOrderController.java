package com.care.module.trade.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.trade.entity.ServiceOrder;
import com.care.module.trade.service.ServiceOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "Trade-Service-Order")
@RestController
@RequestMapping("/api/trade/service-order")
public class ServiceOrderController {

    @Autowired
    private ServiceOrderService serviceOrderService;

    @ApiOperation("Page query service orders")
    @GetMapping("/page")
    public Result<PageResult<ServiceOrder>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String elderName) {
        IPage<ServiceOrder> page = serviceOrderService.getPage(pageNum, pageSize, orderNo, status, elderName);
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("Add service order")
    @PostMapping
    public Result<Void> add(@RequestBody ServiceOrder order) {
        serviceOrderService.add(order);
        return Result.ok("Add success");
    }

    @ApiOperation("Update order status")
    @PutMapping("/status/{id}")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        serviceOrderService.updateStatus(id, status);
        return Result.ok("Update success");
    }

    @ApiOperation("Cancel order")
    @PutMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable Long id) {
        serviceOrderService.cancel(id);
        return Result.ok("Cancel success");
    }

    @ApiOperation("Assign worker to order")
    @PutMapping("/assign/{id}")
    public Result<Void> assignWorker(@PathVariable Long id, @RequestParam Long nurseId, @RequestParam String nurseName) {
        serviceOrderService.assignWorker(id, nurseId, nurseName);
        return Result.ok("派单成功");
    }

    @ApiOperation("Start service")
    @PutMapping("/start/{id}")
    public Result<Void> startService(@PathVariable Long id) {
        serviceOrderService.updateStatus(id, 3);
        return Result.ok("开始服务");
    }

    @ApiOperation("Complete service")
    @PutMapping("/complete/{id}")
    public Result<Void> completeService(@PathVariable Long id) {
        serviceOrderService.completeService(id);
        return Result.ok("服务完成");
    }

    @ApiOperation("Get order detail")
    @GetMapping("/{id}")
    public Result<ServiceOrder> getById(@PathVariable Long id) {
        return Result.success(serviceOrderService.getById(id));
    }
}
