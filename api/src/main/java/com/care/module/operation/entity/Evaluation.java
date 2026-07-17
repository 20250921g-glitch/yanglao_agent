package com.care.module.operation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("evaluation")
public class Evaluation implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String type;
    private String content;
    private Integer score;
    private Long elderId;
    private String elderName;
    private String evaluator;
    private String conclusion;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
