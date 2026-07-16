package com.care.module.operation.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.operation.entity.ActivityRegistration;
import com.care.module.operation.service.ActivityRegistrationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "运营管理-活动报名")
@RestController
@RequestMapping("/api/operation/activity-registration")
public class ActivityRegistrationController {

    @Autowired
    private ActivityRegistrationService registrationService;

    @ApiOperation("分页查询报名信息")
    @GetMapping("/page")
    public Result<PageResult<ActivityRegistration>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long activityId,
            @RequestParam(required = false) Integer status) {
        IPage<ActivityRegistration> result = registrationService.getPage(pageNum, pageSize, activityId, status);
        return Result.success(PageResult.of(result));
    }

    @ApiOperation("根据ID查询报名")
    @GetMapping("/{id}")
    public Result<ActivityRegistration> getById(@PathVariable Long id) {
        return Result.success(registrationService.getById(id));
    }

    @ApiOperation("新增报名")
    @PostMapping
    public Result<?> add(@RequestBody ActivityRegistration registration) {
        registrationService.save(registration);
        return Result.success("报名成功");
    }

    @ApiOperation("更新报名状态")
    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        ActivityRegistration registration = registrationService.getById(id);
        registration.setStatus(status);
        registrationService.updateById(registration);
        return Result.success("状态更新成功");
    }

    @ApiOperation("删除报名")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        registrationService.removeById(id);
        return Result.success("删除成功");
    }
}
