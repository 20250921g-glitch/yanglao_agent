package com.care.module.user.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsService.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /** 短信开发模式：true=在接口响应中返回明文验证码便于联调/演示；false=生产模式，验证码改走网关，绝不返回明文 */
    @Value("${sms.dev-mode:true}")
    private boolean devMode;

    private static final long CODE_TTL_MIN = 5;
    private static final long COOLDOWN_MIN = 1;

    /**
     * 发送短信验证码。
     * 生成的验证码始终写入 Redis（无论开发/生产），供后续 verify 校验。
     * - 开发模式(devMode=true)：直接返回验证码，便于联调与答辩演示。
     * - 生产模式(devMode=false)：不返回明文，改为调用短信网关发送，避免验证码泄露。
     */
    public String send(String phone) {
        Object cooldown = redisTemplate.opsForValue().get("sms:cooldown:" + phone);
        if (cooldown != null) {
            throw new RuntimeException("验证码发送过于频繁，请1分钟后再试");
        }
        String code = String.format("%06d", new Random().nextInt(1000000));
        redisTemplate.opsForValue().set("sms:code:" + phone, code, CODE_TTL_MIN, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set("sms:cooldown:" + phone, "1", COOLDOWN_MIN, TimeUnit.MINUTES);
        if (devMode) {
            // 开发/演示环境：返回明文验证码，方便联调
            return code;
        }
        // 生产环境：通过短信网关下发，严禁在响应中返回明文验证码
        sendViaGateway(phone, code);
        return null;
    }

    /**
     * 调用真实短信网关（阿里云/腾讯云等）。
     * 当前为接入位占位：真实环境应在此处调用厂商 SDK 把 code 发送到 phone。
     * 参数可从 SmsConfig 读取 accessKey/accessSecret。
     */
    private void sendViaGateway(String phone, String code) {
        // TODO 接入真实短信网关（阿里云/腾讯云 SMS），例如：
        //   aliSmsClient.send(phone, "您的验证码为 " + code + "，5 分钟内有效");
        log.info("[SMS-GATEWAY-STUB] 应向 {} 发送验证码 {}（生产环境请接入真实网关）", phone, code);
    }

    public boolean verify(String phone, String code) {
        if (phone == null || code == null || code.trim().isEmpty()) {
            return false;
        }
        Object o = redisTemplate.opsForValue().get("sms:code:" + phone);
        if (o == null) {
            return false;
        }
        boolean ok = o.toString().equals(code.trim());
        if (ok) {
            redisTemplate.delete("sms:code:" + phone);
        }
        return ok;
    }
}
