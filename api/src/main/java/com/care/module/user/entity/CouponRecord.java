package com.care.module.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("coupon_record")
public class CouponRecord implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long couponId;
    private Long userId;
    private Long orderId;
    private Integer status;        // 0未使用 1已使用 2已过期
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime receiveTime;
    private LocalDateTime useTime;

    // 非数据库字段
    @TableField(exist = false)
    private String couponName;
    @TableField(exist = false)
    private String userName;
}
