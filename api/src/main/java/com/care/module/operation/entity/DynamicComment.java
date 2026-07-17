package com.care.module.operation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("dynamic_comment")
public class DynamicComment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long dynamicId;
    private Long userId;
    private String userName;
    private String content;
    private Integer status;
    private LocalDateTime createTime;
}
