package com.care.module.service.worker.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("audit_record")
public class AuditRecord implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long workerId;          // 服务人员ID
    private String auditType;       // 审核类型
    private Integer status;         // 状态:0待审核1通过2拒绝
    private String rejectReason;    // 拒绝原因
    private Long auditorId;         // 审核人ID
    private String auditorName;     // 审核人姓名
    private LocalDateTime auditTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // 非数据库字段
    @TableField(exist = false)
    private String workerName;
}
