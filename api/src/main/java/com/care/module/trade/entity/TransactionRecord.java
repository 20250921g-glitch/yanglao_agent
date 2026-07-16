package com.care.module.trade.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("transaction_record")
public class TransactionRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;
    private String type;
    private String category;
    private BigDecimal amount;
    private BigDecimal balance;
    private String remark;
    private LocalDateTime createTime;
}
