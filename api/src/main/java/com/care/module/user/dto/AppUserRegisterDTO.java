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
}
