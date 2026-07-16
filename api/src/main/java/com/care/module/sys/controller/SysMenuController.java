package com.care.module.sys.controller;

import com.care.common.result.Result;
import com.care.module.sys.entity.SysMenu;
import com.care.module.sys.service.SysMenuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "System-Menu")
@RestController
@RequestMapping("/api/sys/menu")
public class SysMenuController {

    @Autowired
    private SysMenuService sysMenuService;

    @ApiOperation("Get menu tree")
    @GetMapping("/tree")
    public Result<List<SysMenu>> getTree() {
        return Result.success(sysMenuService.getTree());
    }

    @ApiOperation("Get user menus")
    @GetMapping("/user/{userId}")
    public Result<List<SysMenu>> getUserMenus(@PathVariable Long userId) {
        return Result.success(sysMenuService.getUserMenus(userId));
    }

    @ApiOperation("Get all menus")
    @GetMapping("/list")
    public Result<List<SysMenu>> getAll() {
        return Result.success(sysMenuService.list());
    }

    @ApiOperation("Add menu")
    @PostMapping
    public Result<Void> add(@RequestBody SysMenu menu) {
        sysMenuService.add(menu);
        return Result.ok("Add success");
    }

    @ApiOperation("Update menu")
    @PutMapping
    public Result<Void> update(@RequestBody SysMenu menu) {
        sysMenuService.updateMenu(menu);
        return Result.ok("Update success");
    }

    @ApiOperation("Delete menu")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysMenuService.delete(id);
        return Result.ok("Delete success");
    }
}
