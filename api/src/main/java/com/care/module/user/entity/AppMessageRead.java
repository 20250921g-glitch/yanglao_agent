package com.care.module.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * C 端用户对消息的已读 / 隐藏状态（每个用户对每条广播消息独立一行）。
 */
@Data
@TableName("app_message_read")
public class AppMessageRead implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long messageId;
    private Long userId;
    private LocalDateTime readTime;   // 不为空即已读
    private Integer hidden;           // 1=用户已删除（隐藏），0=正常
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
