package com.care.module.operation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("activity_field")
public class ActivityField implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String label;
    private String type;
    private String options;
    private Integer required; // 1必填 0选填
    private Integer status; // 1启用 0停用
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
