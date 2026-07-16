package com.care.module.trade.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("service_order")
public class ServiceOrder implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long elderId;
    private String elderName;
    private String serviceType;
    private String serviceName;
    private Long nurseId;
    private String nurseName;
    private Integer status;  // 1待接单 2已接单 3服务中 4已完成 5已取消
    private BigDecimal price;
    private LocalDateTime appointmentTime;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
