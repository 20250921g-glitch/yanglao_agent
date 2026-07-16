package com.care.module.operation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("salary_record")
public class SalaryRecord implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long workerId;
    private String workerName;
    private String month;  // 2026-07
    private BigDecimal baseSalary;
    private BigDecimal commissionTotal;
    private BigDecimal bonus;
    private BigDecimal deduction;
    private BigDecimal totalSalary;
    private Integer status;  // 0待发放 1已发放
    private LocalDateTime payTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
