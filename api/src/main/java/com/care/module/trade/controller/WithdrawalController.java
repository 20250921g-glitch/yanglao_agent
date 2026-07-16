package com.care.module.trade.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.care.common.result.Result;
import com.care.common.result.PageResult;
import com.care.module.trade.entity.Withdrawal;
import com.care.module.trade.service.WithdrawalService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@Api(tags = "提现管理")
@RestController
@RequestMapping("/api/trade/withdrawal")
public class WithdrawalController {

    private final WithdrawalService withdrawalService;

    public WithdrawalController(WithdrawalService withdrawalService) {
        this.withdrawalService = withdrawalService;
    }

    @GetMapping("/page")
    @ApiOperation("提现分页列表")
    public Result<PageResult<Withdrawal>> page(
            @RequestParam(defaultValue = "1") @ApiParam("页码") Integer pageNum,
            @RequestParam(defaultValue = "10") @ApiParam("每页数量") Integer pageSize,
            @RequestParam(required = false) @ApiParam("服务人员姓名") String workerName,
            @RequestParam(required = false) @ApiParam("状态") Integer status) {
        Page<Withdrawal> page = new Page<>(pageNum, pageSize);
        return Result.success(PageResult.of(withdrawalService.page(page, workerName, status)));
    }

    @GetMapping("/{id}")
    @ApiOperation("提现详情")
    public Result<Withdrawal> detail(@PathVariable Long id) {
        Withdrawal w = withdrawalService.getById(id);
        return w != null ? Result.success(w) : Result.error("记录不存在");
    }

    @PutMapping("/{id}/audit")
    @ApiOperation("审核提现")
    public Result<Void> audit(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Integer status = (Integer) params.get("status");
        String rejectReason = (String) params.get("rejectReason");
        Long auditorId = params.get("auditorId") != null
                ? Long.valueOf(params.get("auditorId").toString()) : null;
        String auditorName = (String) params.get("auditorName");
        boolean ok = withdrawalService.audit(id, status, rejectReason, auditorId, auditorName);
        return ok ? Result.ok("审核成功") : Result.error("记录不存在");
    }
}
