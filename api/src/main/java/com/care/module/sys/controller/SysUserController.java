package com.care.module.sys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.common.util.JwtUtil;
import com.care.module.sys.entity.SysUser;
import com.care.module.sys.service.PermissionService;
import com.care.module.sys.service.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Api(tags = "系统管理-用户管理")
@RestController
@RequestMapping("/api/sys/user")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PermissionService permissionService;

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody SysUser user) {
        SysUser loginUser = sysUserService.login(user.getUsername(), user.getPassword());
        String token = jwtUtil.generateToken(loginUser.getId(), loginUser.getUsername());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", loginUser);
        data.put("roles", permissionService.getUserRoles(loginUser.getId()));
        return Result.success(data);
    }

    @ApiOperation("获取当前用户信息")
    @GetMapping("/info")
    public Result<Map<String, Object>> getUserInfo(HttpServletRequest request) {
        String token = request.getHeader("token");
        if (token == null) {
            token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        SysUser user = sysUserService.getById(userId);
        user.setPassword(null);
        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("roles", permissionService.getUserRoles(userId));
        data.put("menuIds", permissionService.getUserMenuIds(userId));
        return Result.success(data);
    }

    @ApiOperation("修改密码")
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(HttpServletRequest request, @RequestBody Map<String, String> body) {
        String token = request.getHeader("token");
        if (token == null) {
            token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        sysUserService.resetPassword(userId, oldPassword, newPassword);
        return Result.ok("密码修改成功");
    }

    @ApiOperation("分页查询用户")
    @GetMapping("/page")
    public Result<PageResult<SysUser>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName) {
        IPage<SysUser> page = sysUserService.getPage(pageNum, pageSize, username, realName);
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("新增用户")
    @PostMapping
    public Result<Void> add(@RequestBody SysUser user) {
        sysUserService.add(user);
        return Result.ok("新增成功");
    }

    @ApiOperation("修改用户")
    @PutMapping
    public Result<Void> update(@RequestBody SysUser user) {
        sysUserService.updateUser(user);
        return Result.ok("修改成功");
    }

    @ApiOperation("删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysUserService.delete(id);
        return Result.ok("删除成功");
    }

    @ApiOperation("获取用户详情")
    @GetMapping("/{id}")
    public Result<SysUser> getById(@PathVariable Long id) {
        return Result.success(sysUserService.getById(id));
    }
}
