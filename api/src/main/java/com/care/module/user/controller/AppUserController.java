package com.care.module.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.user.entity.AppUser;
import com.care.module.user.entity.UserTag;
import com.care.module.user.service.AppUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "用户管理-APP用户")
@RestController
@RequestMapping("/api/user")
public class AppUserController {

    @Autowired
    private AppUserService appUserService;

    @ApiOperation("分页查询用户")
    @GetMapping("/page")
    public Result<PageResult<AppUser>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        IPage<AppUser> page = appUserService.getPage(pageNum, pageSize, username, realName, phone, status, startDate, endDate);
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("获取用户详情")
    @GetMapping("/{id}")
    public Result<AppUser> getById(@PathVariable Long id) {
        return Result.success(appUserService.getDetail(id));
    }

    @ApiOperation("新增用户")
    @PostMapping
    public Result<Void> add(@RequestBody AppUser user) {
        appUserService.add(user);
        return Result.ok("新增成功");
    }

    @ApiOperation("修改用户")
    @PutMapping
    public Result<Void> update(@RequestBody AppUser user) {
        appUserService.updateUser(user);
        return Result.ok("修改成功");
    }

    @ApiOperation("启用/禁用用户")
    @PutMapping("/status/{id}")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        appUserService.updateStatus(id, status);
        return Result.ok(status == 1 ? "启用成功" : "禁用成功");
    }

    @ApiOperation("删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        appUserService.delete(id);
        return Result.ok("删除成功");
    }

    @ApiOperation("获取用户标签")
    @GetMapping("/{id}/tags")
    public Result<List<UserTag>> getUserTags(@PathVariable Long id) {
        return Result.success(appUserService.getUserTags(id));
    }

    @ApiOperation("更新用户标签")
    @PutMapping("/{id}/tags")
    public Result<Void> updateUserTags(@PathVariable Long id, @RequestBody List<Long> tagIds) {
        appUserService.updateUserTags(id, tagIds);
        return Result.ok("标签更新成功");
    }
}
