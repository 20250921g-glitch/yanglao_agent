package com.care.module.operation.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.operation.entity.DynamicComment;
import com.care.module.operation.service.DynamicCommentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "评论管理")
@RestController
@RequestMapping("/api/operation/comment")
public class CommentController {

    @Autowired
    private DynamicCommentService dynamicCommentService;

    @ApiOperation("评论分页")
    @GetMapping("/page")
    public Result<PageResult<DynamicComment>> getPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        Page<DynamicComment> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<DynamicComment> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(DynamicComment::getStatus, status);
        }
        wrapper.orderByDesc(DynamicComment::getCreateTime);
        IPage<DynamicComment> result = dynamicCommentService.page(pageParam, wrapper);
        return Result.success(PageResult.of(result));
    }

    @ApiOperation("启用/禁用")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        DynamicComment comment = dynamicCommentService.getById(id);
        if (comment == null) {
            return Result.error("评论不存在");
        }
        comment.setStatus(status);
        dynamicCommentService.updateById(comment);
        return Result.success();
    }

    @ApiOperation("删除评论")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        dynamicCommentService.removeById(id);
        return Result.success();
    }
}
