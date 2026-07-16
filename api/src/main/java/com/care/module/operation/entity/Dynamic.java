package com.care.module.operation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("dynamic")
public class Dynamic {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String userName;
    private String title;
    private String content;
    private String images;
    private Integer status;
    private Integer viewCount;
    private Integer likeCount;
    private String auditRemark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // ===== P4-3 数据层补齐字段 =====
    private String topic;          // 话题
    private String userPhone;      // 发布人手机号
    private Integer collectCount;  // 收藏数
    private Integer shareCount;    // 分享数
    private Integer commentCount;  // 评论数

    @TableField(exist = false)
    private String statusText;
}