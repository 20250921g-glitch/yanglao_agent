package com.care.module.service.worker.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.trade.entity.CommissionRecord;
import com.care.module.service.worker.service.CommissionRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "佣金记录管理")
@RestController
@RequestMapping("/api/service/commission")
public class CommissionRecordController {

    @Autowired
    private CommissionRecordService commissionRecordService;

    @ApiOperation("分页查询佣金记录")
    @GetMapping("/page")
    public Result<PageResult<CommissionRecord>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long workerId,
            @RequestParam(required = false) String workerName,
            @RequestParam(required = false) String serviceType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        IPage<CommissionRecord> page = commissionRecordService.getPage(pageNum, pageSize,
                workerId, workerName, status, startDate, endDate);
        return Result.success(PageResult.of(page));
    }
}
