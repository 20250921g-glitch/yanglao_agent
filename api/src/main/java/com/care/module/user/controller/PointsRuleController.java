package com.care.module.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.user.entity.PointsRule;
import com.care.module.user.service.PointsRuleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "积分规则配置")
@RestController
@RequestMapping("/api/point-rule")
public class PointsRuleController {

    @Autowired
    private PointsRuleService pointsRuleService;

    @ApiOperation("积分规则分页")
    @GetMapping("/page")
    public Result<PageResult<PointsRule>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String ruleName,
            @RequestParam(required = false) String actionType) {
        IPage<PointsRule> page = pointsRuleService.getPage(pageNum, pageSize, ruleName, actionType);
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("详情")
    @GetMapping("/{id}")
    public Result<PointsRule> getById(@PathVariable Long id) {
        return Result.success(pointsRuleService.getById(id));
    }

    @ApiOperation("新增")
    @PostMapping
    public Result<Void> add(@RequestBody PointsRule rule) {
        boolean ok = pointsRuleService.save(rule);
        return ok ? Result.ok("新增成功") : Result.error("新增失败");
    }

    @ApiOperation("修改")
    @PutMapping
    public Result<Void> update(@RequestBody PointsRule rule) {
        boolean ok = pointsRuleService.updateById(rule);
        return ok ? Result.ok("修改成功") : Result.error("修改失败");
    }

    @ApiOperation("删除")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean ok = pointsRuleService.removeById(id);
        return ok ? Result.ok("删除成功") : Result.error("删除失败");
    }
}
