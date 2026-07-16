package com.care.module.health.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("elder_family")
public class ElderFamily implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long elderId;
    private String familyName;
    private String relation;
    private String phone;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
