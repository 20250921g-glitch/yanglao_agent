package com.care.module.trade.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.care.common.result.Result;
import com.care.common.result.PageResult;
import com.care.module.trade.entity.OrderEvaluation;
import com.care.module.trade.service.OrderEvaluationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@Api(tags = "订单评价")
@RestController
@RequestMapping("/api/trade/evaluation")
public class OrderEvaluationController {

    private final OrderEvaluationService evaluationService;

    public OrderEvaluationController(OrderEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @GetMapping("/page")
    @ApiOperation("评价分页列表")
    public Result<PageResult<OrderEvaluation>> page(
            @RequestParam(defaultValue = "1") @ApiParam("页码") Integer pageNum,
            @RequestParam(defaultValue = "10") @ApiParam("每页数量") Integer pageSize,
            @RequestParam(required = false) @ApiParam("订单编号") String orderNo,
            @RequestParam(required = false) @ApiParam("评分") Integer score,
            @RequestParam(required = false) @ApiParam("状态") Integer status) {
        Page<OrderEvaluation> page = new Page<>(pageNum, pageSize);
        return Result.success(PageResult.of(evaluationService.page(page, orderNo, score, status)));
    }

    @GetMapping("/{id}")
    @ApiOperation("评价详情")
    public Result<OrderEvaluation> detail(@PathVariable Long id) {
        OrderEvaluation eval = evaluationService.getById(id);
        return eval != null ? Result.success(eval) : Result.error("记录不存在");
    }

    @PostMapping
    @ApiOperation("新增评价")
    public Result<Void> save(@RequestBody OrderEvaluation evaluation) {
        boolean ok = evaluationService.saveEval(evaluation);
        return ok ? Result.ok("添加成功") : Result.error("添加失败");
    }

    @PutMapping("/{id}")
    @ApiOperation("修改评价")
    public Result<Void> update(@PathVariable Long id, @RequestBody OrderEvaluation evaluation) {
        evaluation.setId(id);
        boolean ok = evaluationService.updateEval(evaluation);
        return ok ? Result.ok("修改成功") : Result.error("修改失败");
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除评价")
    public Result<Void> delete(@PathVariable Long id) {
        boolean ok = evaluationService.deleteById(id);
        return ok ? Result.ok("删除成功") : Result.error("删除失败");
    }

    @PutMapping("/{id}/reply")
    @ApiOperation("回复评价")
    public Result<Void> reply(@PathVariable Long id, @RequestBody Map<String, String> params) {
        String reply = params.get("reply");
        boolean ok = evaluationService.reply(id, reply);
        return ok ? Result.ok("回复成功") : Result.error("记录不存在");
    }

    @PutMapping("/{id}/status")
    @ApiOperation("显示/隐藏评价")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        Integer status = params.get("status");
        boolean ok = evaluationService.updateStatus(id, status);
        return ok ? Result.ok("状态更新成功") : Result.error("记录不存在");
    }
}
