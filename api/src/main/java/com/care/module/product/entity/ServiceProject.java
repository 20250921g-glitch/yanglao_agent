package com.care.module.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("service_project")
public class ServiceProject implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String category;
    private Integer duration;
    private BigDecimal price;
    private String method;
    private Integer status; // 1上架 0下架
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
