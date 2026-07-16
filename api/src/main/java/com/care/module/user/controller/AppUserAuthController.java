package com.care.module.user.controller;

import com.care.common.result.Result;
import com.care.common.util.JwtUtil;
import com.care.module.user.dto.AppUserLoginDTO;
import com.care.module.user.dto.AppUserRegisterDTO;
import com.care.module.user.dto.AppUserUpdateDTO;
import com.care.module.user.entity.AppUser;
import com.care.module.user.service.AppUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/app/user")
@Api(tags = "C端用户-注册登录")
public class AppUserAuthController {

    @Autowired
    private AppUserService appUserService;

    @Autowired
    private JwtUtil jwtUtil;

    @ApiOperation("普通用户注册（需图形验证码 + 短信验证码，注册成功自动登录）")
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody AppUserRegisterDTO dto) {
        try {
            AppUser user = appUserService.register(dto);
            // 注册成功直接签发 token，实现「注册即登录」，前端无需再走一次登录
            String token = jwtUtil.generateToken(user.getId(), user.getPhone());
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", user);
            return Result.success(data);
        } catch (RuntimeException e) {
            // 业务校验失败（手机号已注册/验证码错误等）返回 400，避免统一 500 误导
            return Result.error(400, e.getMessage());
        }
    }

    @ApiOperation("普通用户登录（需图形验证码）")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody AppUserLoginDTO dto) {
        try {
            AppUser user = appUserService.login(dto);
            String token = jwtUtil.generateToken(user.getId(), user.getPhone());
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", user);
            return Result.success(data);
        } catch (RuntimeException e) {
            // 业务校验失败（禁用/锁定/验证码错/密码错/账号不存在）返回 400，避免统一 500 误导
            return Result.error(400, e.getMessage());
        }
    }

    @ApiOperation("获取当前登录用户信息")
    @GetMapping("/info")
    public Result<AppUser> info(HttpServletRequest request) {
        String token = request.getHeader("token");
        if (token == null) {
            token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
        }
        if (token == null || token.isEmpty()) {
            return Result.unauthorized("未登录");
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        AppUser user = appUserService.getById(userId);
        return Result.success(user);
    }

    @ApiOperation("修改当前登录用户资料（手机号/密码/状态/积分等不可改）")
    @PostMapping("/update")
    public Result<AppUser> update(@RequestBody AppUserUpdateDTO dto, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        AppUser existing = appUserService.getById(userId);
        if (existing == null) {
            return Result.error("用户不存在");
        }
        AppUser updated = new AppUser();
        updated.setId(userId);
        updated.setUsername(dto.getUsername());
        updated.setAvatar(dto.getAvatar());
        updated.setRealName(dto.getRealName());
        updated.setGender(dto.getGender());
        updated.setBirthDate(dto.getBirthDate());
        updated.setIdCard(dto.getIdCard());
        updated.setNation(dto.getNation());
        updated.setNativePlace(dto.getNativePlace());
        updated.setMarriage(dto.getMarriage());
        updated.setEducation(dto.getEducation());
        updated.setOccupation(dto.getOccupation());
        updated.setWorkUnit(dto.getWorkUnit());
        updated.setHeight(dto.getHeight());
        updated.setWeight(dto.getWeight());
        updated.setAddress(dto.getAddress());
        updated.setEmergencyContact(dto.getEmergencyContact());
        updated.setEmergencyPhone(dto.getEmergencyPhone());
        updated.setBio(dto.getBio());
        appUserService.updateUser(updated);
        // 返回更新后的完整信息（密码为 WRITE_ONLY，不会序列化出去）
        AppUser result = appUserService.getById(userId);
        return Result.success(result);
    }

    /** 从 JWT 过滤器注入的请求属性中取当前用户 id（与 mall/message 等 C 端控制器一致） */
    private Long currentUserId(HttpServletRequest request) {
        Object v = request.getAttribute("userId");
        if (v == null) {
            return null;
        }
        if (v instanceof Long) {
            return (Long) v;
        }
        if (v instanceof Integer) {
            return ((Integer) v).longValue();
        }
        if (v instanceof String) {
            return Long.valueOf((String) v);
        }
        return null;
    }
}
