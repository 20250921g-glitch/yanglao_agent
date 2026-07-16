package com.care.module.service.worker.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.service.worker.entity.TipRecord;
import com.care.module.service.worker.service.TipRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "打赏记录管理")
@RestController
@RequestMapping("/api/service/tip")
public class TipRecordController {

    @Autowired
    private TipRecordService tipRecordService;

    @ApiOperation("分页查询打赏记录")
    @GetMapping("/page")
    public Result<PageResult<TipRecord>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long workerId,
            @RequestParam(required = false) String workerName,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        IPage<TipRecord> page = tipRecordService.getPage(pageNum, pageSize,
                workerId, workerName, startDate, endDate);
        return Result.success(PageResult.of(page));
    }
}
