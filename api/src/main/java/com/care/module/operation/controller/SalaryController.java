package com.care.module.operation.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.operation.entity.SalaryRecord;
import com.care.module.operation.mapper.SalaryRecordMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Api(tags = "Operation-Salary")
@RestController
@RequestMapping("/api/operation/salary")
public class SalaryController {

    @Autowired
    private SalaryRecordMapper salaryRecordMapper;

    @ApiOperation("Page query salary records")
    @GetMapping("/page")
    public Result<PageResult<SalaryRecord>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String workerName,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) Integer status) {
        Page<SalaryRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SalaryRecord> wrapper = new LambdaQueryWrapper<>();
        if (workerName != null && !workerName.isEmpty()) {
            wrapper.like(SalaryRecord::getWorkerName, workerName);
        }
        if (month != null && !month.isEmpty()) {
            wrapper.eq(SalaryRecord::getMonth, month);
        }
        if (status != null) {
            wrapper.eq(SalaryRecord::getStatus, status);
        }
        wrapper.orderByDesc(SalaryRecord::getCreateTime);
        IPage<SalaryRecord> result = salaryRecordMapper.selectPage(page, wrapper);
        return Result.success(PageResult.of(result));
    }

    @ApiOperation("Get worker salary by month")
    @GetMapping("/worker/{workerId}")
    public Result<SalaryRecord> getByWorkerAndMonth(
            @PathVariable Long workerId,
            @RequestParam String month) {
        LambdaQueryWrapper<SalaryRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SalaryRecord::getWorkerId, workerId)
               .eq(SalaryRecord::getMonth, month);
        SalaryRecord record = salaryRecordMapper.selectOne(wrapper);
        return Result.success(record);
    }

    @ApiOperation("Pay salary")
    @PutMapping("/pay/{id}")
    public Result<Void> pay(@PathVariable Long id) {
        SalaryRecord record = salaryRecordMapper.selectById(id);
        if (record == null) {
            return Result.error("工资记录不存在");
        }
        if (record.getStatus() == 1) {
            return Result.error("该工资已发放");
        }
        record.setStatus(1);
        record.setPayTime(LocalDateTime.now());
        salaryRecordMapper.updateById(record);
        return Result.ok("发放成功");
    }

    @ApiOperation("Update salary record")
    @PutMapping
    public Result<Void> update(@RequestBody SalaryRecord record) {
        // Recalculate total salary
        if (record.getBaseSalary() != null && record.getCommissionTotal() != null
            && record.getBonus() != null && record.getDeduction() != null) {
            record.setTotalSalary(
                record.getBaseSalary()
                    .add(record.getCommissionTotal())
                    .add(record.getBonus())
                    .subtract(record.getDeduction())
            );
        }
        salaryRecordMapper.updateById(record);
        return Result.ok("更新成功");
    }
}
