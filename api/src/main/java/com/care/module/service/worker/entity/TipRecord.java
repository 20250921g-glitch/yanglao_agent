package com.care.module.service.worker.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tip_record")
public class TipRecord implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long workerId;         // 服务人员ID
    private String workerName;     // 服务人员姓名
    private Long userId;           // 打赏用户ID
    private Long orderId;          // 关联工单ID
    private BigDecimal amount;     // 打赏金额
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
