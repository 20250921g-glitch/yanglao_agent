package com.care.module.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.user.entity.AppMessage;
import com.care.module.user.service.AppMessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "用户管理-APP消息")
@RestController
@RequestMapping("/api/user/message")
public class AppMessageController {

    @Autowired
    private AppMessageService appMessageService;

    @ApiOperation("分页查询消息")
    @GetMapping("/page")
    public Result<PageResult<AppMessage>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer status) {
        IPage<AppMessage> page = appMessageService.getPage(pageNum, pageSize, title, type, status);
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("发送消息")
    @PostMapping
    public Result<Void> send(@RequestBody AppMessage message) {
        appMessageService.send(message);
        return Result.ok("发送成功");
    }

    @ApiOperation("删除消息")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        appMessageService.delete(id);
        return Result.ok("删除成功");
    }
}
