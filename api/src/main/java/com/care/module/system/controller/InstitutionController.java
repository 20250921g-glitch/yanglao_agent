package com.care.module.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.system.entity.Institution;
import com.care.module.system.service.InstitutionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "机构管理")
@RestController
@RequestMapping("/api/system/institution")
public class InstitutionController {

    @Autowired
    private InstitutionService institutionService;

    @ApiOperation("机构列表")
    @GetMapping("/page")
    public Result<PageResult<Institution>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer status) {
        IPage<Institution> page = institutionService.getPage(pageNum, pageSize, type, status);
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("机构列表(不分页)")
    @GetMapping("/list")
    public Result<List<Institution>> getAll(@RequestParam(required = false) Integer status) {
        return Result.success(institutionService.getAll(status));
    }

    @ApiOperation("机构详情")
    @GetMapping("/{id}")
    public Result<Institution> getById(@PathVariable Long id) {
        Institution institution = institutionService.getById(id);
        if (institution != null) {
            institutionService.fillText(institution);
        }
        return Result.success(institution);
    }

    @ApiOperation("新增机构")
    @PostMapping
    public Result<Void> add(@RequestBody Institution institution) {
        if (institution.getStatus() == null) {
            institution.setStatus(1);
        }
        institutionService.save(institution);
        return Result.ok("新增成功");
    }

    @ApiOperation("更新机构")
    @PutMapping
    public Result<Void> update(@RequestBody Institution institution) {
        institutionService.updateById(institution);
        return Result.ok("更新成功");
    }

    @ApiOperation("删除机构")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        institutionService.removeById(id);
        return Result.ok("删除成功");
    }

    @ApiOperation("修改状态")
    @PutMapping("/status/{id}")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        Institution institution = institutionService.getById(id);
        if (institution == null) {
            return Result.error("记录不存在");
        }
        institution.setStatus(status);
        institutionService.updateById(institution);
        return Result.ok("状态已修改");
    }
}