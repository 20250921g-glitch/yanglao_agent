package com.care.module.service.worker.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("service_worker_tag_relation")
public class ServiceWorkerTagRelation implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long workerId;
    private Long tagId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
