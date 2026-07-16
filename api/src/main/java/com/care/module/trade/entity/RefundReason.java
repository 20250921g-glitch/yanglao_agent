package com.care.module.trade.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("refund_reason")
public class RefundReason {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String reason;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
}
