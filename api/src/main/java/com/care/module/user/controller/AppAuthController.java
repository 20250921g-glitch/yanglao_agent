package com.care.module.user.controller;

import com.care.common.result.Result;
import com.care.module.user.service.CaptchaService;
import com.care.module.user.service.SmsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/app")
@Api(tags = "C端-公共")
public class AppAuthController {

    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private SmsService smsService;

    @ApiOperation("获取图形验证码（返回 captchaKey + base64 图片）")
    @GetMapping("/captcha")
    public Result<Map<String, String>> captcha() {
        return Result.success(captchaService.generate());
    }

    @ApiOperation("发送短信验证码（开发模式在响应中返回明文验证码便于联调；生产环境由 sms.dev-mode=false 关闭明文返回，改走短信网关）")
    @PostMapping("/sms/send")
    public Result<Map<String, String>> sendSms(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        if (phone == null || phone.trim().isEmpty()) {
            return Result.error("手机号不能为空");
        }
        String code = smsService.send(phone.trim());
        Map<String, String> data = new HashMap<>();
        data.put("phone", phone);
        if (code != null) {
            // 仅开发模式返回明文验证码
            data.put("devCode", code);
        } else {
            data.put("message", "验证码已发送，请注意查收短信");
        }
        return Result.success(data);
    }
}
