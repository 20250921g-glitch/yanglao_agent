package com.care.module.user.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * C端普通用户自助修改资料 DTO（仅允许修改个人资料字段，手机号/密码/状态/积分等不可改）
 */
@Data
public class AppUserUpdateDTO {
    private String username;        // 昵称
    private String avatar;          // 头像URL
    private String realName;        // 真实姓名
    private Integer gender;         // 0女 1男
    private LocalDate birthDate;    // 生日
    private String idCard;          // 身份证号
    private String nation;          // 民族
    private String nativePlace;     // 籍贯
    private String marriage;        // 婚姻状况
    private String education;       // 学历
    private String occupation;      // 职业
    private String workUnit;        // 工作单位
    private BigDecimal height;      // 身高(cm)
    private BigDecimal weight;      // 体重(kg)
    private String address;         // 联系地址
    private String emergencyContact;// 紧急联系人
    private String emergencyPhone;  // 紧急联系电话
    private String bio;             // 个人简介
}
