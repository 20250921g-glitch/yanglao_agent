package com.care.module.health.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.health.entity.HealthRecord;
import com.care.module.health.service.AiAdviceCacheService;
import com.care.module.health.service.HealthRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Api(tags = "Health-Record")
@RestController
@RequestMapping("/api/health/record")
public class HealthRecordController {

    @Autowired
    private HealthRecordService recordService;
    @Autowired
    private AiAdviceCacheService aiAdviceCacheService;

    @ApiOperation("Page query health records")
    @GetMapping("/page")
    public Result<PageResult<HealthRecord>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long elderId,
            @RequestParam(required = false) String recordType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        IPage<HealthRecord> page = recordService.getPage(pageNum, pageSize, elderId, recordType, startDate, endDate);
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("Query by elder id")
    @GetMapping("/list/{elderId}")
    public Result<List<HealthRecord>> getByElderId(@PathVariable Long elderId) {
        return Result.success(recordService.getByElderId(elderId));
    }

    @ApiOperation("Query by type")
    @GetMapping("/list")
    public Result<List<HealthRecord>> getByType(
            @RequestParam(required = false) Long elderId,
            @RequestParam(required = false) String recordType) {
        return Result.success(recordService.getByType(elderId, recordType));
    }

    @ApiOperation("Get stats by type")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats(
            @RequestParam(required = false) Long elderId,
            @RequestParam(required = false) String recordType) {
        return Result.success(recordService.getStats(elderId, recordType));
    }

    @ApiOperation("Add health record")
    @PostMapping
    public Result<Void> add(@RequestBody HealthRecord record) {
        recordService.add(record);
        // 档案变更后使相关 AI 健康建议缓存失效（用户再次生成时将重新调用 DeepSeek 取最新数据）
        if (record.getElderId() != null) {
            aiAdviceCacheService.evictForElder(record.getElderId());
        }
        return Result.ok("Add success");
    }

    @ApiOperation("Delete health record")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        HealthRecord record = recordService.getById(id);
        recordService.delete(id);
        if (record != null && record.getElderId() != null) {
            aiAdviceCacheService.evictForElder(record.getElderId());
        }
        return Result.ok("Delete success");
    }
}
