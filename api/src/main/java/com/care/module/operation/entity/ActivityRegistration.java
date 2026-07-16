package com.care.module.operation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("activity_registration")
public class ActivityRegistration implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long activityId;
    private String activityName;
    private Long userId;
    private String userName;
    private String phone;
    private Integer status;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
