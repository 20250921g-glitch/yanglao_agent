package com.care.module.sys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.sys.entity.SysRole;
import com.care.module.sys.service.SysRoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "System-Role")
@RestController
@RequestMapping("/api/sys/role")
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService;

    @ApiOperation("Page query roles")
    @GetMapping("/page")
    public Result<PageResult<SysRole>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String roleName) {
        IPage<SysRole> page = sysRoleService.getPage(pageNum, pageSize, roleName);
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("Get all roles")
    @GetMapping("/list")
    public Result<List<SysRole>> getAll() {
        return Result.success(sysRoleService.getAll());
    }

    @ApiOperation("Add role")
    @PostMapping
    @PreAuthorize("hasRole('ROLE_1')")
    public Result<Void> add(@RequestBody SysRole role) {
        sysRoleService.add(role);
        return Result.ok("Add success");
    }

    @ApiOperation("Update role")
    @PutMapping
    @PreAuthorize("hasRole('ROLE_1')")
    public Result<Void> update(@RequestBody SysRole role) {
        sysRoleService.updateRole(role);
        return Result.ok("Update success");
    }

    @ApiOperation("Delete role")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_1')")
    public Result<Void> delete(@PathVariable Long id) {
        sysRoleService.delete(id);
        return Result.ok("Delete success");
    }

    @ApiOperation("Get role detail")
    @GetMapping("/{id}")
    public Result<SysRole> getById(@PathVariable Long id) {
        return Result.success(sysRoleService.getById(id));
    }

    @ApiOperation("Get role menu ids")
    @GetMapping("/{id}/menus")
    public Result<List<Long>> getRoleMenuIds(@PathVariable Long id) {
        return Result.success(sysRoleService.getRoleMenuIds(id));
    }

    @ApiOperation("Save role menus")
    @PostMapping("/{id}/menus")
    @PreAuthorize("hasRole('ROLE_1')")
    public Result<Void> saveRoleMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        sysRoleService.saveRoleMenus(id, menuIds);
        return Result.ok("Save success");
    }
}
