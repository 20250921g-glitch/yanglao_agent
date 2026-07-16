package com.care.module.health.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("elder")
public class Elder implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer gender;  // 1男 2女
    private Integer age;
    private String idCard;
    private String phone;
    private String address;
    private Integer healthLevel;  // 1自理 2半失能 3失能
    private String emergencyContact;
    private String emergencyPhone;
    private String avatar;
    private Integer status;  // 1正常 0离世/退出
    @TableField("app_user_id")
    private Long appUserId;  // 关联的普通用户ID（C端健康档案归属）
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
