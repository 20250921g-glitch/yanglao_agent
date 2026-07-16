package com.care.module.health.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("health_record")
public class HealthRecord implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long elderId;
    private String elderName;
    private String recordType;
    private String recordValue;
    private LocalDateTime recordTime;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
