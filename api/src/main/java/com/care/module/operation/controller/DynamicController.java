package com.care.module.operation.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.operation.entity.Dynamic;
import com.care.module.operation.service.DynamicService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "动态管理")
@RestController
@RequestMapping("/api/operation/dynamic")
public class DynamicController {

    @Autowired
    private DynamicService dynamicService;

    @ApiOperation("动态列表")
    @GetMapping("/page")
    public Result<PageResult<Dynamic>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String startCreateTime,
            @RequestParam(required = false) String endCreateTime) {
        IPage<Dynamic> page = dynamicService.getPage(pageNum, pageSize, status, userId, startCreateTime, endCreateTime);
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("动态详情")
    @GetMapping("/{id}")
    public Result<Dynamic> getById(@PathVariable Long id) {
        Dynamic dynamic = dynamicService.getById(id);
        if (dynamic != null) {
            dynamicService.fillStatusText(dynamic);
        }
        return Result.success(dynamic);
    }

    @ApiOperation("审核通过")
    @PutMapping("/approve/{id}")
    public Result<Void> approve(@PathVariable Long id, @RequestParam(required = false) String remark) {
        boolean ok = dynamicService.audit(id, 1, remark);
        return ok ? Result.ok("审核通过") : Result.error("操作失败");
    }

    @ApiOperation("审核拒绝")
    @PutMapping("/reject/{id}")
    public Result<Void> reject(@PathVariable Long id, @RequestParam String remark) {
        boolean ok = dynamicService.audit(id, 2, remark);
        return ok ? Result.ok("已拒绝") : Result.error("操作失败");
    }

    @ApiOperation("下架动态")
    @PutMapping("/offline/{id}")
    public Result<Void> offline(@PathVariable Long id) {
        Dynamic dynamic = dynamicService.getById(id);
        if (dynamic == null) {
            return Result.error("记录不存在");
        }
        dynamic.setStatus(3);
        dynamicService.updateById(dynamic);
        return Result.ok("已下架");
    }

    @ApiOperation("发布动态")
    @PostMapping
    public Result<Void> publish(@RequestBody Dynamic dynamic) {
        boolean ok = dynamicService.publish(dynamic);
        return ok ? Result.ok("发布成功") : Result.error("发布失败");
    }

    @ApiOperation("删除动态")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        dynamicService.removeById(id);
        return Result.ok("删除成功");
    }
}