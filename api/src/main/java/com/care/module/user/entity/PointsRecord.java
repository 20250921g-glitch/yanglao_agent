package com.care.module.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("points_record")
public class PointsRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String userName;
    private String type;
    private String source;
    private BigDecimal amount;
    private BigDecimal balance;
    private String remark;
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String typeText;
    @TableField(exist = false)
    private String sourceText;
}