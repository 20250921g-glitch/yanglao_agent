package com.care.module.operation.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.operation.entity.ActivityField;
import com.care.module.operation.service.ActivityFieldService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "运营管理-活动字段")
@RestController
@RequestMapping("/api/operation/activity-field")
public class ActivityFieldController {

    @Autowired
    private ActivityFieldService activityFieldService;

    @ApiOperation("分页查询活动字段")
    @GetMapping("/page")
    public Result<PageResult<ActivityField>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        IPage<ActivityField> page = activityFieldService.getPage(pageNum, pageSize, keyword);
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("新增字段")
    @PostMapping
    public Result<Void> add(@RequestBody ActivityField f) {
        activityFieldService.saveField(f);
        return Result.ok("新增成功");
    }

    @ApiOperation("修改字段")
    @PutMapping
    public Result<Void> update(@RequestBody ActivityField f) {
        activityFieldService.saveField(f);
        return Result.ok("修改成功");
    }

    @ApiOperation("启用/停用字段")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        activityFieldService.updateStatus(id, status);
        return Result.ok("状态已更新");
    }

    @ApiOperation("删除字段")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        activityFieldService.removeById(id);
        return Result.ok("删除成功");
    }
}
