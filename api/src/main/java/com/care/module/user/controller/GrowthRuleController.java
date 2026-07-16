package com.care.module.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.user.entity.GrowthRule;
import com.care.module.user.service.GrowthRuleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "用户管理-成长值规则")
@RestController
@RequestMapping("/api/user/growth-rule")
public class GrowthRuleController {

    @Autowired
    private GrowthRuleService growthRuleService;

    @ApiOperation("分页查询成长值规则")
    @GetMapping("/page")
    public Result<PageResult<GrowthRule>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        IPage<GrowthRule> page = growthRuleService.getPage(pageNum, pageSize, keyword);
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("新增规则")
    @PostMapping
    public Result<Void> add(@RequestBody GrowthRule rule) {
        growthRuleService.saveRule(rule);
        return Result.ok("新增成功");
    }

    @ApiOperation("修改规则")
    @PutMapping
    public Result<Void> update(@RequestBody GrowthRule rule) {
        growthRuleService.saveRule(rule);
        return Result.ok("修改成功");
    }

    @ApiOperation("启用/停用规则")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        growthRuleService.updateStatus(id, status);
        return Result.ok(status == 1 ? "启用成功" : "停用成功");
    }

    @ApiOperation("删除规则")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        growthRuleService.deleteRule(id);
        return Result.ok("删除成功");
    }
}
