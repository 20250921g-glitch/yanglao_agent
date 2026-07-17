package com.care.module.user.dto;

import lombok.Data;

@Data
public class AppUserRegisterDTO {
    private String phone;
    private String password;
    private String nickname;
    private String smsCode;
    private String captchaKey;
    private String captchaCode;
    // 用户类型: ELDER老人 / FAMILY家属 / VOLUNTEER志愿者 / STAFF工作人员，不传默认 FAMILY
    private String role;
}
