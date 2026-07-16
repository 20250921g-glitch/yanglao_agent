package com.care.module.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("product_category")
public class ProductCategory implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;           // 分类名称
    private Long parentId;         // 父级ID（预留多级分类）
    private String serviceType;    // 服务类型：家政护理/康复理疗/上门体检
    private Integer sort;          // 排序
    private Integer status;        // 状态：1启用 0禁用
    private String icon;           // 图标
    private String description;    // 描述
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
