package com.care.module.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.user.entity.MemberLevel;
import com.care.module.user.service.MemberLevelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "用户管理-会员等级")
@RestController
@RequestMapping("/api/user/level")
public class MemberLevelController {

    @Autowired
    private MemberLevelService memberLevelService;

    @ApiOperation("分页查询会员等级")
    @GetMapping("/page")
    public Result<PageResult<MemberLevel>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String levelName) {
        IPage<MemberLevel> page = memberLevelService.getPage(pageNum, pageSize, levelName);
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("全部会员等级列表")
    @GetMapping("/list")
    public Result<List<MemberLevel>> getAllList() {
        return Result.success(memberLevelService.getAllList());
    }

    @ApiOperation("新增会员等级")
    @PostMapping
    public Result<Void> add(@RequestBody MemberLevel level) {
        memberLevelService.add(level);
        return Result.ok("新增成功");
    }

    @ApiOperation("修改会员等级")
    @PutMapping
    public Result<Void> update(@RequestBody MemberLevel level) {
        memberLevelService.updateLevel(level);
        return Result.ok("修改成功");
    }

    @ApiOperation("删除会员等级")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        memberLevelService.delete(id);
        return Result.ok("删除成功");
    }
}
