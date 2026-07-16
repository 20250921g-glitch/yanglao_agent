package com.care.module.operation.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.trade.entity.CommissionRecord;
import com.care.module.trade.mapper.CommissionRecordMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Api(tags = "Operation-Commission")
@RestController
@RequestMapping("/api/operation/commission")
public class CommissionController {

    @Autowired
    private CommissionRecordMapper commissionRecordMapper;

    @ApiOperation("Page query commission records")
    @GetMapping("/page")
    public Result<PageResult<CommissionRecord>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String workerName,
            @RequestParam(required = false) Integer status) {
        Page<CommissionRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CommissionRecord> wrapper = new LambdaQueryWrapper<>();
        if (workerName != null && !workerName.isEmpty()) {
            wrapper.like(CommissionRecord::getWorkerName, workerName);
        }
        if (status != null) {
            wrapper.eq(CommissionRecord::getStatus, status);
        }
        wrapper.orderByDesc(CommissionRecord::getCreateTime);
        IPage<CommissionRecord> result = commissionRecordMapper.selectPage(page, wrapper);
        return Result.success(PageResult.of(result));
    }

    @ApiOperation("Settle commission")
    @PutMapping("/settle/{id}")
    public Result<Void> settle(@PathVariable Long id) {
        CommissionRecord record = commissionRecordMapper.selectById(id);
        if (record == null) {
            return Result.error("佣金记录不存在");
        }
        if (record.getStatus() == 1) {
            return Result.error("该佣金已结算");
        }
        record.setStatus(1);
        record.setSettleTime(LocalDateTime.now());
        commissionRecordMapper.updateById(record);
        return Result.ok("结算成功");
    }

    @ApiOperation("Batch settle commissions")
    @PutMapping("/batch-settle")
    public Result<Void> batchSettle(@RequestBody Long[] ids) {
        for (Long id : ids) {
            CommissionRecord record = commissionRecordMapper.selectById(id);
            if (record != null && record.getStatus() == 0) {
                record.setStatus(1);
                record.setSettleTime(LocalDateTime.now());
                commissionRecordMapper.updateById(record);
            }
        }
        return Result.ok("批量结算成功");
    }
}
