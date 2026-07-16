package com.care.module.service.worker.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.service.worker.entity.ServiceOrderSetting;
import com.care.module.service.worker.service.ServiceOrderSettingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "工单设置管理")
@RestController
@RequestMapping("/api/service/setting")
public class ServiceOrderSettingController {

    @Autowired
    private ServiceOrderSettingService settingService;

    @ApiOperation("查询所有工单设置")
    @GetMapping("/list")
    public Result<List<ServiceOrderSetting>> getList() {
        return Result.success(settingService.list());
    }

    @ApiOperation("分页查询工单设置")
    @GetMapping("/page")
    public Result<PageResult<ServiceOrderSetting>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        IPage<ServiceOrderSetting> page = settingService.page(
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize));
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("获取工单设置详情")
    @GetMapping("/{id}")
    public Result<ServiceOrderSetting> getById(@PathVariable Long id) {
        return Result.success(settingService.getById(id));
    }

    @ApiOperation("新增工单设置")
    @PostMapping
    public Result<Void> add(@RequestBody ServiceOrderSetting setting) {
        settingService.save(setting);
        return Result.ok("新增成功");
    }

    @ApiOperation("修改工单设置")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody ServiceOrderSetting setting) {
        setting.setId(id);
        settingService.updateSetting(setting);
        return Result.ok("修改成功");
    }

    @ApiOperation("删除工单设置")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        settingService.removeById(id);
        return Result.ok("删除成功");
    }
}
