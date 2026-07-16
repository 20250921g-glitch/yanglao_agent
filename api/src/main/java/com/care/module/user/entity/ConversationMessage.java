package com.care.module.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("conversation_message")
public class ConversationMessage implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long conversationId;
    private Integer senderType; // 1用户 2客服
    private String content;
    private LocalDateTime createTime;
}
