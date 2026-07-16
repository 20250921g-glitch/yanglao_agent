package com.care.module.operation.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.operation.entity.Activity;
import com.care.module.operation.service.ActivityService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "运营管理-活动管理")
@RestController
@RequestMapping("/api/operation/activity")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @ApiOperation("分页查询活动")
    @GetMapping("/page")
    public Result<PageResult<Activity>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String startUpdateTime,
            @RequestParam(required = false) String endUpdateTime) {
        IPage<Activity> result = activityService.getPage(pageNum, pageSize, name, status, category, location, startUpdateTime, endUpdateTime);
        return Result.success(PageResult.of(result));
    }

    @ApiOperation("根据ID查询活动")
    @GetMapping("/{id}")
    public Result<Activity> getById(@PathVariable Long id) {
        return Result.success(activityService.getById(id));
    }

    @ApiOperation("新增活动")
    @PostMapping
    public Result<?> add(@RequestBody Activity activity) {
        activityService.add(activity);
        return Result.success("添加成功");
    }

    @ApiOperation("更新活动")
    @PutMapping
    public Result<?> update(@RequestBody Activity activity) {
        activityService.updateActivity(activity);
        return Result.success("更新成功");
    }

    @ApiOperation("删除活动")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        activityService.delete(id);
        return Result.success("删除成功");
    }

    @ApiOperation("获取活动列表（不分页）")
    @GetMapping("/list")
    public Result<java.util.List<Activity>> getList() {
        return Result.success(activityService.list());
    }
}
