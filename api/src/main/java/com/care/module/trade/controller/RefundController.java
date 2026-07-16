package com.care.module.trade.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.trade.entity.Refund;
import com.care.module.trade.service.RefundService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@Api(tags = "退款管理")
@RestController
@RequestMapping("/api/trade/refund")
public class RefundController {

    private final RefundService refundService;

    public RefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    @GetMapping("/page")
    @ApiOperation("退款分页列表")
    public Result<PageResult<Refund>> page(
            @RequestParam(defaultValue = "1") @ApiParam("页码") Integer pageNum,
            @RequestParam(defaultValue = "10") @ApiParam("每页数量") Integer pageSize,
            @RequestParam(required = false) @ApiParam("售后单号") String refundNo,
            @RequestParam(required = false) @ApiParam("状态") Integer status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        Page<Refund> page = new Page<>(pageNum, pageSize);
        return Result.success(PageResult.of(refundService.page(page, refundNo, status, startDate, endDate)));
    }

    @GetMapping("/{id}")
    @ApiOperation("退款详情")
    public Result<Refund> detail(@PathVariable Long id) {
        Refund refund = refundService.getById(id);
        return refund != null ? Result.success(refund) : Result.error("记录不存在");
    }

    @PostMapping
    @ApiOperation("新增退款申请")
    public Result<Void> save(@RequestBody Refund refund) {
        refundService.saveRefund(refund);
        return Result.ok("添加成功");
    }

    @PutMapping("/approve/{id}")
    @ApiOperation("审核通过")
    public Result<Void> approve(@PathVariable Long id,
                                @RequestParam String auditor,
                                @RequestParam(required = false) String remark) {
        boolean ok = refundService.approve(id, auditor, remark);
        return ok ? Result.ok("审核通过") : Result.error("记录不存在");
    }

    @PutMapping("/reject/{id}")
    @ApiOperation("审核拒绝")
    public Result<Void> reject(@PathVariable Long id,
                               @RequestParam String auditor,
                               @RequestParam(required = false) String remark) {
        boolean ok = refundService.reject(id, auditor, remark);
        return ok ? Result.ok("已拒绝") : Result.error("记录不存在");
    }

    @PutMapping("/process/{id}")
    @ApiOperation("执行退款")
    public Result<Void> process(@PathVariable Long id) {
        boolean ok = refundService.processRefund(id);
        return ok ? Result.ok("退款成功") : Result.error("记录不存在或状态不允许");
    }

    @PutMapping("/cancel/{id}")
    @ApiOperation("取消退款")
    public Result<Void> cancel(@PathVariable Long id) {
        boolean ok = refundService.cancel(id);
        return ok ? Result.ok("已取消") : Result.error("记录不存在或状态不允许");
    }

    @PutMapping("/{id}/audit")
    @ApiOperation("审核退款(通用)")
    public Result<Void> audit(@PathVariable Long id,
                              @RequestParam Integer status,
                              @RequestParam(required = false) java.math.BigDecimal actualAmount,
                              @RequestParam(required = false) String handleRemark,
                              @RequestParam(required = false) Long handlerId,
                              @RequestParam(required = false) String handlerName) {
        boolean ok = refundService.audit(id, status, actualAmount, handleRemark, handlerId, handlerName);
        return ok ? Result.ok("审核成功") : Result.error("记录不存在");
    }
}
