package com.care.module.operation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("activity")
public class Activity implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String type;
    private String location;
    private String imageUrl;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String description;
    private Integer participantCount;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // ===== P4-3 数据层补齐字段 =====
    private String updateBy;   // 最后更新人
}
