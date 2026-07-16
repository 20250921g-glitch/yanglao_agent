package com.care.module.trade.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.trade.entity.ProductOrder;
import com.care.module.trade.entity.ProductOrderDetailVO;
import com.care.module.trade.service.ProductOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/trade/product-order")
@Api(tags = "Trade-Product-Order")
public class ProductOrderController {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private ProductOrderService productOrderService;

    @ApiOperation("Page query product orders")
    @GetMapping("/page")
    public Result<PageResult<ProductOrder>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        IPage<ProductOrder> page = productOrderService.getPage(pageNum, pageSize, orderNo, status, keyword, startDate, endDate);
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("Add product order")
    @PostMapping
    public Result<Void> add(@RequestBody ProductOrder order) {
        productOrderService.add(order);
        return Result.ok("Add success");
    }

    @ApiOperation("Update order status")
    @PutMapping("/status/{id}")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        productOrderService.updateStatus(id, status);
        return Result.ok("Update success");
    }

    @ApiOperation("Update order price")
    @PutMapping("/price/{id}")
    public Result<Void> updatePrice(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Object priceObj = body.get("price");
        BigDecimal price = priceObj == null ? null : new BigDecimal(priceObj.toString());
        String remark = body.get("remark") == null ? null : body.get("remark").toString();
        productOrderService.updatePrice(id, price, remark);
        return Result.ok("Price updated");
    }

    @ApiOperation("Close order")
    @PutMapping("/close/{id}")
    public Result<Void> close(@PathVariable Long id) {
        productOrderService.close(id);
        return Result.ok("Order closed");
    }

    @ApiOperation("手动派单：指派服务人员，订单流转为待服务")
    @PutMapping("/dispatch/{id}")
    public Result<Void> dispatch(@PathVariable Long id,
                                @RequestParam Long workerId,
                                @RequestParam String workerName,
                                @RequestParam(required = false) String appointmentTime,
                                @RequestParam(required = false, defaultValue = "admin") String operator) {
        LocalDateTime at = null;
        if (appointmentTime != null && !appointmentTime.isEmpty()) {
            at = LocalDateTime.parse(appointmentTime, DTF);
        }
        productOrderService.dispatch(id, workerId, workerName, at, operator);
        return Result.ok("派单成功");
    }

    @ApiOperation("Get order detail")
    @GetMapping("/{id}")
    public Result<ProductOrderDetailVO> getById(@PathVariable Long id) {
        return Result.success(productOrderService.getDetail(id));
    }
}
