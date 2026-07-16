package com.care.module.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("app_message")
public class AppMessage implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    private String type;           // 消息类型:系统通知/活动推送
    private String target;          // 发送对象:all全部  指定用户
    private LocalDateTime sendTime;
    private Integer status;         // 1已发送 0草稿
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // ===== C端非数据库字段 =====
    @TableField(exist = false)
    private Boolean read;           // 当前用户是否已读
}
