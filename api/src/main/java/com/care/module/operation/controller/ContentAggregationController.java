package com.care.module.operation.controller;

import com.care.common.result.Result;
import com.care.module.operation.mapper.ContentStatMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/content")
public class ContentAggregationController {

    @Autowired
    private ContentStatMapper statMapper;

    @GetMapping("/aggregation")
    public Result<List<Map<String, Object>>> aggregation() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(withType("dynamic", "动态", "operation/dynamic", statMapper.statDynamic()));
        list.add(withType("banner", "轮播图", "operation/banner", statMapper.statBanner()));
        list.add(withType("food", "食物", "operation/food", statMapper.statFood()));
        list.add(withType("health_news", "健康资讯", "operation/health-news", statMapper.statHealthNews()));
        list.add(withType("institution", "养老机构", "operation/institution", statMapper.statInstitution()));
        list.add(withType("recipe", "食谱", "operation/recipe", statMapper.statRecipe()));
        list.add(withType("video", "视频", "operation/video", statMapper.statVideo()));
        list.add(withType("activity", "活动", "operation/activity", statMapper.statActivity()));
        return Result.success(list);
    }

    private Map<String, Object> withType(String type, String name, String path, Map<String, Object> m) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", type);
        map.put("typeName", name);
        map.put("path", path);
        map.put("total", m.get("total"));
        map.put("enabled", m.get("enabled"));
        map.put("disabled", m.get("disabled"));
        map.put("latestTime", m.get("latestTime"));
        map.put("latestTitle", m.get("latestTitle"));
        return map;
    }
}
