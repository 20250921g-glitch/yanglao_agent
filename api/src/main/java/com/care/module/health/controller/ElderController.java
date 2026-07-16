package com.care.module.health.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.health.entity.Elder;
import com.care.module.health.service.ElderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "健康管理-老人档案")
@RestController
@RequestMapping("/api/health/elder")
public class ElderController {

    @Autowired
    private ElderService elderService;

    @ApiOperation("分页查询老人")
    @GetMapping("/page")
    public Result<PageResult<Elder>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer healthLevel) {
        IPage<Elder> page = elderService.getPage(pageNum, pageSize, name, phone, healthLevel);
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("获取所有老人（下拉框用）")
    @GetMapping("/list")
    public Result<List<Elder>> getAll() {
        return Result.success(elderService.getAll());
    }

    @ApiOperation("新增老人")
    @PostMapping
    public Result<Void> add(@RequestBody Elder elder) {
        elderService.add(elder);
        return Result.ok("新增成功");
    }

    @ApiOperation("修改老人")
    @PutMapping
    public Result<Void> update(@RequestBody Elder elder) {
        elderService.updateElder(elder);
        return Result.ok("修改成功");
    }

    @ApiOperation("删除老人")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        elderService.delete(id);
        return Result.ok("删除成功");
    }

    @ApiOperation("获取老人详情")
    @GetMapping("/{id}")
    public Result<Elder> getById(@PathVariable Long id) {
        return Result.success(elderService.getById(id));
    }
}
