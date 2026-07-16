package com.care.module.trade.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("refund")
public class Refund {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String refundNo;
    private Long orderId;
    private String orderNo;
    private Long userId;
    private String userName;
    private String userPhone;
    private BigDecimal refundAmount;
    private BigDecimal actualAmount;
    private String refundReason;
    private String refundDescription;
    private String refundType;
    private Integer status;
    private LocalDateTime handleTime;
    private String handleRemark;
    private Long handlerId;
    private String handlerName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String statusText;
}
