package com.care.module.health.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("disease")
public class Disease {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String category;
    private String symptoms;
    private String treatment;
    private String precautions;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String categoryText;
    @TableField(exist = false)
    private String statusText;
}