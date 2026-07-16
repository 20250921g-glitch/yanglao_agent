package com.care.module.trade.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("withdrawal")
public class Withdrawal {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String withdrawNo;
    private Long workerId;
    private String workerName;
    private BigDecimal amount;
    private String withdrawType;
    private String bankCard;
    private String bankName;
    private Integer status;
    private String rejectReason;
    private LocalDateTime auditTime;
    private String auditRemark;
    private Long auditorId;
    private String auditorName;
    private LocalDateTime completeTime;
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String statusText;
}
