package com.care.module.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("member_level")
public class MemberLevel implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String levelName;
    private String levelIcon;
    private Integer minScore;
    private Integer maxScore;
    private BigDecimal discount;  // 折扣率
    private String benefits;     // 权益说明
    private Integer status;       // 1启用 0禁用
    private Integer sort;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
