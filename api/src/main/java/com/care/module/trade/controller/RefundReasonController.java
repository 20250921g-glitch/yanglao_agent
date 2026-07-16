package com.care.module.trade.controller;

import com.care.common.result.Result;
import com.care.module.trade.entity.RefundReason;
import com.care.module.trade.service.RefundReasonService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Api(tags = "退款原因")
@RestController
@RequestMapping("/api/trade/refund-reason")
public class RefundReasonController {

    private final RefundReasonService refundReasonService;

    public RefundReasonController(RefundReasonService refundReasonService) {
        this.refundReasonService = refundReasonService;
    }

    @GetMapping("/list")
    @ApiOperation("退款原因列表")
    public Result<List<RefundReason>> list() {
        return Result.success(refundReasonService.listEnabled());
    }

    @GetMapping("/{id}")
    @ApiOperation("退款原因详情")
    public Result<RefundReason> detail(@PathVariable Long id) {
        RefundReason reason = refundReasonService.getById(id);
        return reason != null ? Result.success(reason) : Result.error("记录不存在");
    }

    @PostMapping
    @ApiOperation("新增退款原因")
    public Result<Void> save(@RequestBody RefundReason reason) {
        boolean ok = refundReasonService.saveReason(reason);
        return ok ? Result.ok("添加成功") : Result.error("添加失败");
    }

    @PutMapping("/{id}")
    @ApiOperation("修改退款原因")
    public Result<Void> update(@PathVariable Long id, @RequestBody RefundReason reason) {
        reason.setId(id);
        boolean ok = refundReasonService.updateReason(reason);
        return ok ? Result.ok("修改成功") : Result.error("修改失败");
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除退款原因")
    public Result<Void> delete(@PathVariable Long id) {
        boolean ok = refundReasonService.deleteById(id);
        return ok ? Result.ok("删除成功") : Result.error("删除失败");
    }
}
