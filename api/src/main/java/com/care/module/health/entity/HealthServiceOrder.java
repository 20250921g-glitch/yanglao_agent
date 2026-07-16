package com.care.module.health.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("health_service_order")
public class HealthServiceOrder implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long elderId;
    private String elderName;
    private String serviceName;
    private Long workerId;
    private String workerName;
    private LocalDateTime serviceTime;
    private Integer duration;
    private String status;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
