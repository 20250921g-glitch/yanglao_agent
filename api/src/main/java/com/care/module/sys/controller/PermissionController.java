package com.care.module.sys.controller;

import com.care.common.result.Result;
import com.care.module.sys.service.PermissionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "权限管理")
@RestController
@RequestMapping("/api/sys/permission")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @ApiOperation("获取用户角色")
    @GetMapping("/user-roles/{userId}")
    public Result<List<String>> getUserRoles(@PathVariable Long userId) {
        return Result.success(permissionService.getUserRoles(userId));
    }

    @ApiOperation("获取用户菜单ID")
    @GetMapping("/user-menus/{userId}")
    public Result<List<Long>> getUserMenuIds(@PathVariable Long userId) {
        return Result.success(permissionService.getUserMenuIds(userId));
    }

    @ApiOperation("获取用户按钮级权限码")
    @GetMapping("/user-perms/{userId}")
    public Result<List<String>> getUserPerms(@PathVariable Long userId) {
        return Result.success(permissionService.getUserPerms(userId));
    }

    @ApiOperation("分配角色给用户")
    @PutMapping("/assign-roles")
    public Result<Void> assignRoles(@RequestBody Map<String, Object> params) {
        Long userId = Long.parseLong(params.get("userId").toString());
        List<Long> roleIds = ((List<?>) params.get("roleIds")).stream()
            .map(id -> Long.parseLong(id.toString()))
            .collect(java.util.stream.Collectors.toList());
        permissionService.assignRoles(userId, roleIds);
        return Result.ok("分配成功");
    }

    @ApiOperation("分配菜单给角色")
    @PutMapping("/assign-menus")
    public Result<Void> assignMenus(@RequestBody Map<String, Object> params) {
        Long roleId = Long.parseLong(params.get("roleId").toString());
        List<Long> menuIds = ((List<?>) params.get("menuIds")).stream()
            .map(id -> Long.parseLong(id.toString()))
            .collect(java.util.stream.Collectors.toList());
        permissionService.assignMenus(roleId, menuIds);
        return Result.ok("分配成功");
    }
}