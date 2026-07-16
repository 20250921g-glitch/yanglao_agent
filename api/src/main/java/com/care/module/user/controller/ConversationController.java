package com.care.module.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.user.entity.Conversation;
import com.care.module.user.entity.ConversationMessage;
import com.care.module.user.service.ConversationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "用户管理-会话")
@RestController
@RequestMapping("/api/user/conversation")
public class ConversationController {

    @Autowired
    private ConversationService conversationService;

    @ApiOperation("分页查询会话")
    @GetMapping("/page")
    public Result<PageResult<Conversation>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        IPage<Conversation> page = conversationService.getPage(pageNum, pageSize, keyword);
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("查询会话消息明细")
    @GetMapping("/{id}/messages")
    public Result<List<Map<String, Object>>> getMessages(@PathVariable Long id) {
        List<ConversationMessage> list = conversationService.getMessages(id);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
        List<Map<String, Object>> result = new ArrayList<>();
        for (ConversationMessage m : list) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("from", m.getSenderType() != null && m.getSenderType() == 2 ? "kefu" : "user");
            item.put("text", m.getContent());
            item.put("time", m.getCreateTime() == null ? "" : m.getCreateTime().format(dtf));
            result.add(item);
        }
        return Result.success(result);
    }

    @ApiOperation("删除会话")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        conversationService.removeById(id);
        return Result.ok("删除成功");
    }

    @ApiOperation("客服发送消息")
    @PostMapping("/{id}/messages")
    public Result<Void> sendMessage(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String content = body == null ? null : body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return Result.error("消息内容不能为空");
        }
        conversationService.sendMessage(id, content.trim());
        return Result.ok("发送成功");
    }
}
