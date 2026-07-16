package com.care.module.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("app_user")
public class AppUser implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("nickname") // 表中列名为 nickname，前端以 username 字段展示为昵称
    private String username;
    @TableField("real_name")
    private String realName;
    private String phone;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private String avatar;
    private Integer gender;  // 0女 1男
    private LocalDate birthDate;
    private String idCard;
    private String nation;
    private String nativePlace;
    private String marriage;
    private String education;
    private String occupation;
    private String workUnit;
    private java.math.BigDecimal height;
    private java.math.BigDecimal weight;
    private Long levelId;
    private String address;
    private String emergencyContact;
    private String emergencyPhone;
    private String bio;
    private Integer status;  // 1正常 0禁用
    @TableField("source")
    private String source;  // 注册来源(APP注册/后台创建)
    @TableField("role")
    private String role;    // C端用户类型: ELDER老人/FAMILY家属/VOLUNTEER志愿者/STAFF工作人员
    @TableField(exist = false)
    private String remark;
    private java.math.BigDecimal points;
    @TableField(exist = false)
    private java.math.BigDecimal growthValue;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 非数据库字段
    @TableField(exist = false)
    private String levelName;
    @TableField(exist = false)
    private List<String> tags;
}
