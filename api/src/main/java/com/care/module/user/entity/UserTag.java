package com.care.module.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("user_tag")
public class UserTag implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String tagName;
    private String tagType;  // 标签类型:客户类型/健康标签
    private String color;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
