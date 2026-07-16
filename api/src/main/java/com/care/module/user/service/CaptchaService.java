package com.care.module.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class CaptchaService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final long TTL_MINUTES = 5;
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int LEN = 4;

    private String createKey() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String createCode() {
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < LEN; i++) {
            sb.append(CHARS.charAt(r.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    private String generateImage(String code) throws Exception {
        int w = 120, h = 42;
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
        Random r = new Random();
        for (int i = 0; i < 8; i++) {
            g.setColor(new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255)));
            g.drawLine(r.nextInt(w), r.nextInt(h), r.nextInt(w), r.nextInt(h));
        }
        g.setFont(new Font("Arial", Font.BOLD, 30));
        for (int i = 0; i < code.length(); i++) {
            g.setColor(new Color(r.nextInt(120), r.nextInt(120), r.nextInt(120)));
            g.drawString(String.valueOf(code.charAt(i)), 16 + i * 26, 32);
        }
        g.dispose();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    public Map<String, String> generate() {
        try {
            String key = createKey();
            String code = createCode();
            String image = generateImage(code);
            redisTemplate.opsForValue().set("captcha:" + key, code, TTL_MINUTES, TimeUnit.MINUTES);
            Map<String, String> m = new HashMap<>();
            m.put("captchaKey", key);
            m.put("captchaImage", image);
            return m;
        } catch (Exception e) {
            throw new RuntimeException("生成验证码失败: " + e.getMessage());
        }
    }

    public boolean validate(String key, String code) {
        if (key == null || code == null || key.trim().isEmpty() || code.trim().isEmpty()) {
            return false;
        }
        Object o = redisTemplate.opsForValue().get("captcha:" + key);
        if (o == null) {
            return false;
        }
        boolean ok = o.toString().equalsIgnoreCase(code.trim());
        if (ok) {
            redisTemplate.delete("captcha:" + key);
        }
        return ok;
    }
}
