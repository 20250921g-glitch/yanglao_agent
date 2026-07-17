package com.care.module.operation.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.operation.entity.Activity;
import com.care.module.operation.entity.ActivityRegistration;
import com.care.module.operation.service.ActivityRegistrationService;
import com.care.module.operation.service.ActivityService;
import com.care.module.user.entity.AppUser;
import com.care.module.user.service.AppUserService;
import com.care.module.user.service.AppMessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "C端-活动报名")
@RestController
@RequestMapping("/api/app/activity")
public class AppActivityController {

    @Autowired
    private ActivityService activityService;
    @Autowired
    private ActivityRegistrationService registrationService;
    @Autowired
    private AppUserService appUserService;
    @Autowired
    private AppMessageService appMessageService;
    @Autowired
    private HttpServletRequest request;

    // 报名记录状态：0待审核 1报名成功 2已拒绝 3已取消

    @ApiOperation("活动列表（C端，只读，默认隐藏已结束）")
    @GetMapping("/list")
    public Result<PageResult<Activity>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        Page<Activity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Activity::getStatus, status);
        } else {
            wrapper.ne(Activity::getStatus, 2); // 默认隐藏已结束
        }
        if (StringUtils.hasText(type)) wrapper.eq(Activity::getType, type);
        if (StringUtils.hasText(keyword)) wrapper.like(Activity::getName, keyword);
        wrapper.orderByDesc(Activity::getCreateTime);
        activityService.page(page, wrapper);
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("活动详情")
    @GetMapping("/{id}")
    public Result<Activity> detail(@PathVariable Long id) {
        return Result.success(activityService.getById(id));
    }

    @ApiOperation("当前用户报名活动")
    @PostMapping("/register")
    @Transactional(rollbackFor = Exception.class)
    public Result<?> register(@RequestBody RegisterDTO dto) {
        Long userId = uid();
        if (userId == null) return Result.unauthorized("请先登录");

        Activity activity = activityService.getById(dto.getActivityId());
        if (activity == null) return Result.error("活动不存在");
        if (activity.getStatus() != null && activity.getStatus() == 2) {
            return Result.error("活动已结束，无法报名");
        }
        // 活动时间已过期（结束时间早于当前时间）则不允许报名
        if (activity.getEndTime() != null && activity.getEndTime().isBefore(LocalDateTime.now())) {
            return Result.error("活动已过期，无法报名");
        }
        // 防重复报名（已取消的可重新报）
        long exists = registrationService.lambdaQuery()
                .eq(ActivityRegistration::getActivityId, dto.getActivityId())
                .eq(ActivityRegistration::getUserId, userId)
                .ne(ActivityRegistration::getStatus, 3)
                .count();
        if (exists > 0) return Result.error("您已报名该活动，无需重复报名");

        AppUser user = appUserService.getById(userId);
        ActivityRegistration reg = new ActivityRegistration();
        reg.setActivityId(activity.getId());
        reg.setActivityName(activity.getName());
        reg.setUserId(userId);
        reg.setUserName(user != null ? user.getUsername() : null);
        reg.setPhone(StringUtils.hasText(dto.getPhone()) && user != null ? dto.getPhone() : (user != null ? user.getPhone() : null));
        reg.setStatus(1); // 报名成功
        reg.setRemark(dto.getRemark());
        registrationService.save(reg);

        // 报名成功后自动给用户发送消息通知（失败不阻断主流程）
        try {
            appMessageService.sendToUser(userId, "活动报名成功",
                    "您已成功报名活动【" + activity.getName() + "】，请留意活动开始时间。", "系统通知");
        } catch (Exception ignored) {
        }

        // 参与人数 +1（非核心操作，失败不影响报名结果）
        try {
            Integer pc = activity.getParticipantCount() == null ? 0 : activity.getParticipantCount();
            activity.setParticipantCount(pc + 1);
            activityService.updateById(activity);
        } catch (Exception ignored) {
        }

        return Result.success("报名成功");
    }

    @ApiOperation("我的报名列表")
    @GetMapping("/my")
    public Result<PageResult<RegistrationVO>> my(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = uid();
        if (userId == null) return Result.unauthorized("请先登录");

        long total = registrationService.lambdaQuery()
                .eq(ActivityRegistration::getUserId, userId)
                .count();
        Page<ActivityRegistration> page = registrationService.lambdaQuery()
                .eq(ActivityRegistration::getUserId, userId)
                .orderByDesc(ActivityRegistration::getCreateTime)
                .page(new Page<>(pageNum, pageSize));

        List<RegistrationVO> vos = page.getRecords().stream().map(r -> {
            RegistrationVO vo = new RegistrationVO();
            vo.setId(r.getId());
            vo.setRegistrationStatus(r.getStatus());
            vo.setRemark(r.getRemark());
            vo.setCreateTime(r.getCreateTime());
            Activity a = activityService.getById(r.getActivityId());
            if (a != null) {
                vo.setActivityId(a.getId());
                vo.setActivityName(a.getName());
                vo.setActivityType(a.getType());
                vo.setActivityLocation(a.getLocation());
                vo.setActivityStartTime(a.getStartTime());
                vo.setActivityEndTime(a.getEndTime());
                vo.setActivityImage(a.getImageUrl());
            }
            return vo;
        }).collect(Collectors.toList());

        return Result.success(new PageResult<>(total, vos));
    }

    @ApiOperation("取消报名")
    @PostMapping("/cancel/{registrationId}")
    public Result<?> cancel(@PathVariable Long registrationId) {
        Long userId = uid();
        if (userId == null) return Result.unauthorized("请先登录");

        ActivityRegistration reg = registrationService.getById(registrationId);
        if (reg == null) return Result.error("报名记录不存在");
        if (!userId.equals(reg.getUserId())) return Result.forbidden("无权操作该报名");
        if (reg.getStatus() != null && reg.getStatus() == 3) {
            return Result.error("已取消，无需重复操作");
        }
        reg.setStatus(3);
        registrationService.updateById(reg);

        Activity a = activityService.getById(reg.getActivityId());
        if (a != null && a.getParticipantCount() != null && a.getParticipantCount() > 0) {
            a.setParticipantCount(a.getParticipantCount() - 1);
            activityService.updateById(a);
        }
        return Result.success("已取消报名");
    }

    private Long uid() {
        Object o = request.getAttribute("userId");
        return o == null ? null : (Long) o;
    }

    @Data
    public static class RegisterDTO {
        private Long activityId;
        private String phone;
        private String remark;
    }

    @Data
    public static class RegistrationVO {
        private Long id;
        private Long activityId;
        private String activityName;
        private String activityType;
        private String activityLocation;
        private LocalDateTime activityStartTime;
        private LocalDateTime activityEndTime;
        private String activityImage;
        private Integer registrationStatus;
        private String remark;
        private LocalDateTime createTime;
    }
}
