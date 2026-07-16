package com.care.module.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("points_rule")
public class PointsRule implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String ruleName;
    private String ruleCode;
    private String actionType; // 获取 / 消费
    private Integer points;
    private Integer limitCount;
    private Integer status; // 1启用 0停用
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
