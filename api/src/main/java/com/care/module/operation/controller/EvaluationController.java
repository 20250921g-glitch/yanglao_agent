package com.care.module.operation.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.operation.entity.Evaluation;
import com.care.module.operation.service.EvaluationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "运营管理-测评管理")
@RestController
@RequestMapping("/api/operation/evaluation")
public class EvaluationController {

    @Autowired
    private EvaluationService evaluationService;

    @ApiOperation("分页查询测评")
    @GetMapping("/page")
    public Result<PageResult<Evaluation>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String elderName) {
        IPage<Evaluation> page = evaluationService.getPage(pageNum, pageSize, title, elderName);
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("新增测评")
    @PostMapping
    public Result<Void> add(@RequestBody Evaluation evaluation) {
        evaluationService.add(evaluation);
        return Result.ok("新增成功");
    }

    @ApiOperation("删除测评")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        evaluationService.delete(id);
        return Result.ok("删除成功");
    }
}
