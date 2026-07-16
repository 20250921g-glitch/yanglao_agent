package com.care.module.service.worker.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.service.worker.entity.AuditRecord;
import com.care.module.service.worker.entity.ServiceWorker;
import com.care.module.service.worker.service.AuditRecordService;
import com.care.module.service.worker.service.ServiceWorkerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@Api(tags = "审核记录管理")
@RestController
@RequestMapping("/api/service/audit")
public class AuditRecordController {

    @Autowired
    private AuditRecordService auditRecordService;
    @Autowired
    private ServiceWorkerService workerService;

    @ApiOperation("分页查询审核记录")
    @GetMapping("/page")
    public Result<PageResult<AuditRecord>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String workerName,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String auditType) {
        IPage<AuditRecord> page = auditRecordService.getPage(pageNum, pageSize, workerName, status, auditType);
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("获取审核详情")
    @GetMapping("/{id}")
    public Result<AuditRecord> getById(@PathVariable Long id) {
        return Result.success(auditRecordService.getByIdWithWorker(id));
    }

    @ApiOperation("执行审核")
    @PutMapping("/{id}/audit")
    public Result<Void> doAudit(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Integer status = (Integer) params.get("status");
        String rejectReason = (String) params.get("rejectReason");
        // 更新服务人员审核状态
        AuditRecord record = auditRecordService.getById(id);
        if (record == null) {
            return Result.error("审核记录不存在");
        }
        workerService.audit(record.getWorkerId(), status, rejectReason);
        // 保存审核记录
        AuditRecord update = new AuditRecord();
        update.setId(id);
        update.setStatus(status);
        update.setRejectReason(rejectReason);
        update.setAuditTime(LocalDateTime.now());
        // 审核人信息可从上下文获取，此处简化处理
        auditRecordService.updateById(update);
        return Result.ok(status == 1 ? "审核通过" : "审核拒绝");
    }
}
