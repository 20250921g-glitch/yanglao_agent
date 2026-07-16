package com.care.module.health.controller;

import com.care.common.ai.DeepSeekService;
import com.care.common.result.Result;
import com.care.module.health.entity.Elder;
import com.care.module.health.entity.HealthRecord;
import com.care.module.health.service.AiAdviceCacheService;
import com.care.module.health.service.ElderService;
import com.care.module.health.service.HealthRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * C 端普通用户 —— AI 健康建议。
 * 路由统一 /api/app/ai，从 request 取 userId，与老人档案归属一致。
 * 无 DeepSeek 密钥时走降级文案，保证接口稳定可用。
 */
@RestController
@RequestMapping("/api/app/ai")
@Api(tags = "C端用户-AI健康建议")
public class AppAiController {

    @Autowired
    private ElderService elderService;
    @Autowired
    private HealthRecordService healthRecordService;
    @Autowired
    private DeepSeekService deepSeekService;
    @Autowired
    private AiAdviceCacheService aiAdviceCacheService;

    @ApiOperation("生成健康建议（基于关联老人真实档案，结果按用户+老人缓存 30 分钟，force=true 忽略缓存重新生成）")
    @PostMapping("/health-advice")
    public Result<Map<String, Object>> healthAdvice(@RequestBody(required = false) Map<String, Object> body,
                                                     HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        Long elderId = parseLong(body == null ? null : body.get("elderId"));
        boolean force = parseBoolean(body == null ? null : body.get("force"));

        List<Elder> elders;
        if (elderId != null) {
            Elder elder = elderService.getById(elderId);
            if (elder == null || elder.getAppUserId() == null || !elder.getAppUserId().equals(userId)) {
                return Result.forbidden("无权查看该档案");
            }
            elders = Arrays.asList(elder);
        } else {
            elders = elderService.lambdaQuery()
                    .eq(Elder::getAppUserId, userId)
                    .orderByDesc(Elder::getId)
                    .list();
        }
        if (elders.isEmpty()) {
            return Result.error("您暂未关联任何健康档案");
        }

        // 1) 非强制时优先读缓存，命中则直接返回（cached=true），避免重复调用 DeepSeek 浪费额度
        if (!force) {
            Map<String, Object> cached = aiAdviceCacheService.getCached(userId, elderId);
            if (cached != null && cached.get("advice") != null) {
                Map<String, Object> m = new HashMap<String, Object>();
                m.put("advice", cached.get("advice"));
                m.put("elderCount", cached.get("elderCount") == null ? elders.size() : cached.get("elderCount"));
                m.put("cached", true);
                return Result.success(m);
            }
        }

        StringBuilder profile = new StringBuilder();
        for (Elder elder : elders) {
            profile.append("【老人】").append(elder.getName() == null ? "" : elder.getName());
            if (elder.getGender() != null) {
                profile.append("，性别：").append(elder.getGender() == 1 ? "男" : "女");
            }
            if (elder.getAge() != null) {
                profile.append("，年龄：").append(elder.getAge()).append("岁");
            }
            if (elder.getHealthLevel() != null) {
                profile.append("，健康等级：").append(elder.getHealthLevel());
            }
            profile.append("\n");
            List<HealthRecord> records = healthRecordService.getByElderId(elder.getId());
            if (records.isEmpty()) {
                profile.append("  暂无健康记录\n");
            } else {
                profile.append("  近期健康记录：\n");
                int limit = Math.min(records.size(), 20);
                for (int i = 0; i < limit; i++) {
                    HealthRecord r = records.get(i);
                    profile.append("  - ").append(r.getRecordType()).append("：").append(r.getRecordValue());
                    if (r.getRecordTime() != null) {
                        profile.append("（").append(r.getRecordTime().toLocalDate()).append("）");
                    }
                    profile.append("\n");
                }
            }
        }

        String system = "你是智慧养老健康助手，依据老人真实健康档案（血压、血糖、体重、睡眠、心率等指标），"
                + "用中文给出简洁、可执行的关怀与健康建议，分条列出，语气温和，不做医疗诊断，提醒如有不适及时就医。";
        String advice = deepSeekService.chat(system, profile.toString());

        // 2) 写入缓存（失败开放，Redis 异常不影响返回）
        aiAdviceCacheService.put(userId, elderId, advice, elders.size());

        Map<String, Object> m = new HashMap<String, Object>();
        m.put("advice", advice);
        m.put("elderCount", elders.size());
        m.put("cached", false);
        return Result.success(m);
    }

    private Long parseLong(Object o) {
        if (o == null) {
            return null;
        }
        try {
            return Long.valueOf(String.valueOf(o));
        } catch (Exception e) {
            return null;
        }
    }

    private boolean parseBoolean(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof Boolean) {
            return (Boolean) o;
        }
        return Boolean.parseBoolean(String.valueOf(o));
    }
}
