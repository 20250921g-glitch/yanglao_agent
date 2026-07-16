package com.care.module.service.worker.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.service.worker.entity.ServiceWorker;
import com.care.module.service.worker.entity.ServiceWorkerTag;
import com.care.module.service.worker.service.ServiceWorkerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "服务人员管理")
@RestController
@RequestMapping("/api/service/worker")
public class ServiceWorkerController {

    @Autowired
    private ServiceWorkerService workerService;

    @ApiOperation("分页查询服务人员")
    @GetMapping("/page")
    public Result<PageResult<ServiceWorker>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String serviceType,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) Integer auditStatus,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        IPage<ServiceWorker> page = workerService.getPage(pageNum, pageSize, name, phone,
                serviceType, tags, auditStatus, status, startDate, endDate);
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("获取服务人员详情")
    @GetMapping("/{id}")
    public Result<ServiceWorker> getById(@PathVariable Long id) {
        return Result.success(workerService.getByIdWithTags(id));
    }

    @ApiOperation("新增服务人员")
    @PostMapping
    public Result<Void> add(@RequestBody ServiceWorker worker) {
        workerService.add(worker);
        return Result.ok("新增成功");
    }

    @ApiOperation("修改服务人员")
    @PutMapping
    public Result<Void> update(@RequestBody ServiceWorker worker) {
        workerService.updateWorker(worker);
        return Result.ok("修改成功");
    }

    @ApiOperation("启用/禁用服务人员")
    @PutMapping("/status/{id}")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        workerService.updateStatus(id, status);
        return Result.ok(status == 1 ? "启用成功" : "禁用成功");
    }

    @ApiOperation("删除服务人员")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        workerService.delete(id);
        return Result.ok("删除成功");
    }

    @ApiOperation("审核服务人员")
    @PutMapping("/audit/{id}")
    public Result<Void> audit(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Integer status = (Integer) params.get("status");
        String rejectReason = (String) params.get("rejectReason");
        workerService.audit(id, status, rejectReason);
        return Result.ok(status == 1 ? "审核通过" : "审核拒绝");
    }

    @ApiOperation("获取服务人员标签")
    @GetMapping("/{id}/tags")
    public Result<List<ServiceWorkerTag>> getTags(@PathVariable Long id) {
        return Result.success(workerService.getTagsByWorkerId(id));
    }

    @ApiOperation("更新服务人员标签")
    @PutMapping("/{id}/tags")
    public Result<Void> updateTags(@PathVariable Long id, @RequestBody Map<String, List<Long>> params) {
        List<Long> tagIds = params.get("tagIds");
        workerService.updateTags(id, tagIds);
        return Result.ok("标签更新成功");
    }

    @ApiOperation("获取服务人员列表（不分页）")
    @GetMapping("/list")
    public Result<List<ServiceWorker>> getList() {
        return Result.success(workerService.getList());
    }
}
