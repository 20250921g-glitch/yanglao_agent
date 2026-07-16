package com.care.module.service.worker.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("service_worker")
public class ServiceWorker implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String phone;
    private String avatar;
    private Integer gender;          // 性别:0女1男
    private String idCard;           // 身份证号
    private String bankCard;         // 银行卡号
    private String bankName;         // 开户行
    private String serviceType;      // 服务类型:家政护工/康复理疗/上门体检
    private String region;           // 负责区域
    private String intro;            // 个人简介
    private String certificate;      // 职业证书URL
    private String joinType;         // 加入方式:服务端注册/APP注册
    private Integer auditStatus;     // 审核状态:0待审核1已通过2已拒绝
    private String auditRemark;      // 审核备注
    private Integer allowTip;        // 允许打赏:1是0否
    private Integer status;          // 状态:1正常0禁用
    private LocalDateTime lastLoginTime;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 非数据库字段
    @TableField(exist = false)
    private List<ServiceWorkerTag> tags;
    @TableField(exist = false)
    private String auditStatusText;
    @TableField(exist = false)
    private String statusText;
    @TableField(exist = false)
    private String serviceTypeText;
}
