package com.care.module.operation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("dynamic_favorite")
public class DynamicFavorite {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long dynamicId;
    private Long userId;
    private LocalDateTime createTime;
}
