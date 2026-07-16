package com.care.module.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("institution")
public class Institution {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String code;
    private String type;
    private String address;
    private String contact;
    private String phone;
    private String email;
    private String license;
    private Integer capacity;
    private Integer staffCount;
    private BigDecimal rating;
    private String description;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String typeText;
    @TableField(exist = false)
    private String statusText;
}