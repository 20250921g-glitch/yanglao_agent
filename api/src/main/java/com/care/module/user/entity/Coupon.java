package com.care.module.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("coupon")
public class Coupon implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer type;         // 1满减券 2折扣券
    private BigDecimal denomination;  // 面额/折扣值
    private BigDecimal minAmount;  // 使用门槛
    private Integer totalCount;    // 发放总量
    private Integer remainCount;   // 剩余数量
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;        // 1生效 0失效
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
