package com.care.module.service.worker.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("service_worker_tag")
public class ServiceWorkerTag implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String tagName;     // 标签名称
    private String serviceType;  // 所属服务类型
    private String color;        // 标签颜色
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
