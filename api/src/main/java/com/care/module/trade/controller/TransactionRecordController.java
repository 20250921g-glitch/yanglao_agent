package com.care.module.trade.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.care.common.result.Result;
import com.care.common.result.PageResult;
import com.care.module.trade.entity.TransactionRecord;
import com.care.module.trade.service.TransactionRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@Api(tags = "收支明细")
@RestController
@RequestMapping("/api/trade/transaction")
public class TransactionRecordController {

    private final TransactionRecordService recordService;

    public TransactionRecordController(TransactionRecordService recordService) {
        this.recordService = recordService;
    }

    @GetMapping("/page")
    @ApiOperation("收支分页列表")
    public Result<PageResult<TransactionRecord>> page(
            @RequestParam(defaultValue = "1") @ApiParam("页码") Integer pageNum,
            @RequestParam(defaultValue = "10") @ApiParam("每页数量") Integer pageSize,
            @RequestParam(required = false) @ApiParam("类型") String type,
            @RequestParam(required = false) @ApiParam("类别") String category,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        Page<TransactionRecord> page = new Page<>(pageNum, pageSize);
        return Result.success(PageResult.of(recordService.page(page, type, category, startDate, endDate)));
    }
}
