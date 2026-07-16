package com.care.module.user.dto;

import lombok.Data;

@Data
public class AppUserLoginDTO {
    private String phone;
    private String password;
    private String captchaKey;
    private String captchaCode;
}
