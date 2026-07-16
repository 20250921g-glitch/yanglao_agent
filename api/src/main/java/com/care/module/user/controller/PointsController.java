package com.care.module.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.user.entity.PointsRecord;
import com.care.module.user.service.PointsRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Api(tags = "积分管理")
@RestController
@RequestMapping("/api/user/points")
public class PointsController {

    @Autowired
    private PointsRecordService pointsRecordService;

    @ApiOperation("积分记录分页")
    @GetMapping("/page")
    public Result<PageResult<PointsRecord>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String type) {
        IPage<PointsRecord> page = pointsRecordService.getPage(pageNum, pageSize, userId, type, null);
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("积分统计")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String type) {
        return Result.success(pointsRecordService.getStats(userId, type));
    }

    @ApiOperation("调整积分")
    @PutMapping("/adjust")
    public Result<Void> adjustPoints(
            @RequestParam Long userId,
            @RequestParam BigDecimal amount,
            @RequestParam(defaultValue = "points") String type,
            @RequestParam(defaultValue = "adjust") String source,
            @RequestParam(required = false) String remark) {
        boolean ok = pointsRecordService.adjustPoints(userId, type, amount, source, remark);
        return ok ? Result.ok("调整成功") : Result.error("操作失败");
    }
}