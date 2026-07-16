package com.care.module.sys.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("staff")
public class Staff implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String staffNo;
    private String dept;
    private String position;
    private String phone;
    private String email;
    private String idCard;
    private String gender;
    private LocalDateTime entryDate;
    private Integer status;
    private String remark;
    private String password;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
