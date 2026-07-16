package com.care.module.sys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.care.common.result.Result;
import com.care.common.result.PageResult;
import com.care.module.sys.entity.SysOperationLog;
import com.care.module.sys.service.SysOperationLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "操作日志")
@RestController
@RequestMapping("/api/system/operation-log")
public class OperationLogController {

    @Autowired
    private SysOperationLogService operationLogService;

    @ApiOperation("分页查询操作日志（真实落库数据）")
    @GetMapping("/page")
    public Result<PageResult<SysOperationLog>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        IPage<SysOperationLog> result = operationLogService.pageQuery(
                pageNum, pageSize, userName, module, operation, startTime, endTime);
        return Result.success(PageResult.of(result));
    }
}
