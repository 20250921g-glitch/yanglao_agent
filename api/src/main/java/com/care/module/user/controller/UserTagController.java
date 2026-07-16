package com.care.module.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.user.entity.UserTag;
import com.care.module.user.service.UserTagService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "用户管理-用户标签")
@RestController
@RequestMapping("/api/user/tag")
public class UserTagController {

    @Autowired
    private UserTagService userTagService;

    @ApiOperation("分页查询标签")
    @GetMapping("/page")
    public Result<PageResult<UserTag>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String tagName,
            @RequestParam(required = false) String tagType) {
        IPage<UserTag> page = userTagService.getPage(pageNum, pageSize, tagName, tagType);
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("全部标签列表")
    @GetMapping("/list")
    public Result<List<UserTag>> getAllList() {
        return Result.success(userTagService.getAllList());
    }

    @ApiOperation("新增标签")
    @PostMapping
    public Result<Void> add(@RequestBody UserTag tag) {
        userTagService.add(tag);
        return Result.ok("新增成功");
    }

    @ApiOperation("修改标签")
    @PutMapping
    public Result<Void> update(@RequestBody UserTag tag) {
        userTagService.updateTag(tag);
        return Result.ok("修改成功");
    }

    @ApiOperation("删除标签")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userTagService.delete(id);
        return Result.ok("删除成功");
    }
}
