package com.care.module.service.worker.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.user.entity.AppMessage;
import com.care.module.user.entity.AppMessageRead;
import com.care.module.user.mapper.AppMessageReadMapper;
import com.care.module.user.service.AppMessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * C 端普通用户 —— 消息通知（收件箱）。
 * app_message 为广播式消息（无 user_id / 已读字段），
 * 每个用户对每条消息的已读/删除状态记录在 app_message_read。
 * 与管理员后台 /api/user/message（消息管理，可群发）完全隔离。
 */
@RestController
@RequestMapping("/api/app/message")
@Api(tags = "C端用户-消息通知")
public class AppUserMessageController {

    @Autowired
    private AppMessageService appMessageService;
    @Autowired
    private AppMessageReadMapper appMessageReadMapper;

    private Long currentUserId(HttpServletRequest request) {
        Object v = request.getAttribute("userId");
        return v == null ? null : (Long) v;
    }

    /** 我对各消息的状态：messageId -> AppMessageRead */
    private Map<Long, AppMessageRead> myReadMap(Long userId) {
        List<AppMessageRead> rows = appMessageReadMapper.selectList(
                new LambdaQueryWrapper<AppMessageRead>().eq(AppMessageRead::getUserId, userId));
        Map<Long, AppMessageRead> map = new HashMap<>();
        for (AppMessageRead r : rows) {
            map.put(r.getMessageId(), r);
        }
        return map;
    }

    /** 已发送、未被我删除的消息（按发送时间倒序），并回填已读标记 */
    private List<AppMessage> visibleMessages(Long userId, String type) {
        LambdaQueryWrapper<AppMessage> w = new LambdaQueryWrapper<>();
        w.eq(AppMessage::getStatus, 1);
        if (type != null && !type.trim().isEmpty()) {
            w.eq(AppMessage::getType, type);
        }
        w.orderByDesc(AppMessage::getSendTime).orderByDesc(AppMessage::getId);
        List<AppMessage> all = appMessageService.list(w);
        Map<Long, AppMessageRead> map = myReadMap(userId);
        List<AppMessage> result = new ArrayList<>();
        for (AppMessage m : all) {
            AppMessageRead r = map.get(m.getId());
            if (r != null && r.getHidden() != null && r.getHidden() == 1) {
                continue; // 已被我删除
            }
            m.setRead(r != null && r.getReadTime() != null);
            result.add(m);
        }
        return result;
    }

    @ApiOperation("我的消息列表")
    @GetMapping("/list")
    public Result<PageResult<AppMessage>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String type,
            HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        List<AppMessage> all = visibleMessages(userId, type);
        long total = all.size();
        int from = (pageNum - 1) * pageSize;
        int to = Math.min(from + pageSize, all.size());
        List<AppMessage> records = from >= all.size() ? new ArrayList<AppMessage>() : all.subList(from, to);
        return Result.success(new PageResult<>(total, records));
    }

    @ApiOperation("未读消息数")
    @GetMapping("/unread-count")
    public Result<Integer> unreadCount(HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        int count = 0;
        for (AppMessage m : visibleMessages(userId, null)) {
            if (m.getRead() == null || !m.getRead()) {
                count++;
            }
        }
        return Result.success(count);
    }

    @ApiOperation("标记已读")
    @PostMapping("/read/{id}")
    public Result<Void> read(@PathVariable Long id, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        AppMessage m = appMessageService.getById(id);
        if (m == null) {
            return Result.notFound("消息不存在");
        }
        upsertRead(id, userId);
        return Result.ok("已读");
    }

    @ApiOperation("全部标记已读")
    @PostMapping("/read-all")
    public Result<Void> readAll(HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        for (AppMessage m : visibleMessages(userId, null)) {
            if (m.getRead() == null || !m.getRead()) {
                upsertRead(m.getId(), userId);
            }
        }
        return Result.ok("已全部标记已读");
    }

    @ApiOperation("删除消息（仅对当前用户隐藏）")
    @PostMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        AppMessageRead r = findRow(id, userId);
        if (r == null) {
            r = new AppMessageRead();
            r.setMessageId(id);
            r.setUserId(userId);
            r.setReadTime(LocalDateTime.now());
            r.setHidden(1);
            appMessageReadMapper.insert(r);
        } else {
            r.setHidden(1);
            if (r.getReadTime() == null) {
                r.setReadTime(LocalDateTime.now());
            }
            appMessageReadMapper.updateById(r);
        }
        return Result.ok("已删除");
    }

    private AppMessageRead findRow(Long messageId, Long userId) {
        return appMessageReadMapper.selectOne(new LambdaQueryWrapper<AppMessageRead>()
                .eq(AppMessageRead::getMessageId, messageId)
                .eq(AppMessageRead::getUserId, userId)
                .last("LIMIT 1"));
    }

    private void upsertRead(Long messageId, Long userId) {
        AppMessageRead r = findRow(messageId, userId);
        if (r == null) {
            r = new AppMessageRead();
            r.setMessageId(messageId);
            r.setUserId(userId);
            r.setReadTime(LocalDateTime.now());
            r.setHidden(0);
            appMessageReadMapper.insert(r);
        } else if (r.getReadTime() == null) {
            r.setReadTime(LocalDateTime.now());
            appMessageReadMapper.updateById(r);
        }
    }
}
