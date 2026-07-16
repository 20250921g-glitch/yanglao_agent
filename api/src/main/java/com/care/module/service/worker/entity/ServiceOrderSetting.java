package com.care.module.service.worker.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("service_order_setting")
public class ServiceOrderSetting implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String serviceType;        // 服务类型
    private BigDecimal commissionRate; // 默认佣金比例
    private BigDecimal minWithdraw;    // 最低提现金额
    private String remark;             // 备注
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
