package com.care.module.sys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.sys.entity.Staff;
import com.care.module.sys.service.StaffService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "系统管理-员工管理")
@RestController
@RequestMapping("/api/system/staff")
public class StaffController {

    @Autowired
    private StaffService staffService;

    @ApiOperation("分页查询员工")
    @GetMapping("/page")
    public Result<PageResult<Staff>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String dept,
            @RequestParam(required = false) Integer status) {
        IPage<Staff> result = staffService.getPage(pageNum, pageSize, name, dept, status);
        return Result.success(PageResult.of(result));
    }

    @ApiOperation("根据ID查询员工")
    @GetMapping("/{id}")
    public Result<Staff> getById(@PathVariable Long id) {
        Staff staff = staffService.getById(id);
        return Result.success(staff);
    }

    @ApiOperation("新增员工")
    @PostMapping
    public Result<?> add(@RequestBody Staff staff) {
        staffService.add(staff);
        return Result.success("添加成功");
    }

    @ApiOperation("更新员工")
    @PutMapping
    public Result<?> update(@RequestBody Staff staff) {
        staffService.updateStaff(staff);
        return Result.success("更新成功");
    }

    @ApiOperation("删除员工")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        staffService.delete(id);
        return Result.success("删除成功");
    }
}
