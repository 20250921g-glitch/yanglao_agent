package com.care.module.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String code;
    private Long categoryId;
    private String categoryName;
    private String serviceType;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stock;
    private Integer sales;
    private String image;
    private String images;
    private String description;
    private String detail;
    private String unit;
    private Integer status;
    private Integer recommend;
    private Integer sort;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // ===== P4-3 数据层补齐字段 =====
    private String updateBy;   // 最后更新人

    @TableField(exist = false)
    private String statusText;
}