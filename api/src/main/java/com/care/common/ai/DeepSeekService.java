package com.care.common.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DeepSeekService {

    @Value("${deepseek.api-key:}")
    private String apiKey;

    @Value("${deepseek.base-url:https://api.deepseek.com/v1}")
    private String baseUrl;

    @Value("${deepseek.model:deepseek-chat}")
    private String model;

    @Autowired
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String chat(String systemPrompt, String userPrompt) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return fallback();
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> sys = new HashMap<String, Object>();
            sys.put("role", "system");
            sys.put("content", systemPrompt);
            Map<String, Object> user = new HashMap<String, Object>();
            user.put("role", "user");
            user.put("content", userPrompt);
            List<Map<String, Object>> messages = new ArrayList<Map<String, Object>>();
            messages.add(sys);
            messages.add(user);

            Map<String, Object> body = new HashMap<String, Object>();
            body.put("model", model);
            body.put("messages", messages);
            body.put("temperature", 0.7);
            body.put("stream", false);

            HttpEntity<Map<String, Object>> request = new HttpEntity<Map<String, Object>>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/chat/completions", request, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode choices = root.get("choices");
                if (choices != null && choices.isArray() && choices.size() > 0) {
                    JsonNode msg = choices.get(0).get("message");
                    if (msg != null) {
                        JsonNode content = msg.get("content");
                        if (content != null) {
                            return content.asText();
                        }
                    }
                }
            }
            return fallback();
        } catch (Exception e) {
            return fallback();
        }
    }

    private String fallback() {
        return "抱歉，当前 AI 健康建议服务暂不可用（未配置密钥或使用量超限），请稍后再试。您可继续查看健康档案中的各项指标数据。";
    }
}
