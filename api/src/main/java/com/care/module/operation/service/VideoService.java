package com.care.module.operation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.operation.entity.Video;
import com.care.module.operation.mapper.VideoMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VideoService extends ServiceImpl<VideoMapper, Video> {

    public IPage<Video> getPage(Integer pageNum, Integer pageSize, String category, Integer status) {
        Page<Video> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<>();
        if (category != null && !category.isEmpty()) {
            wrapper.eq(Video::getCategory, category);
        }
        if (status != null) {
            wrapper.eq(Video::getStatus, status);
        }
        wrapper.orderByDesc(Video::getCreateTime);
        IPage<Video> result = page(page, wrapper);
        result.getRecords().forEach(this::fillText);
        return result;
    }

    public List<Video> getAll(Integer status) {
        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Video::getStatus, status);
        }
        wrapper.orderByDesc(Video::getCreateTime);
        List<Video> list = list(wrapper);
        list.forEach(this::fillText);
        return list;
    }

    public void incrementView(Long id) {
        Video video = getById(id);
        if (video != null) {
            video.setViewCount(video.getViewCount() != null ? video.getViewCount() + 1 : 1);
            updateById(video);
        }
    }

    public void fillText(Video video) {
        Map<String, String> categoryMap = new HashMap<>();
        categoryMap.put("health", "健康科普");
        categoryMap.put("exercise", "运动保健");
        categoryMap.put("cooking", "营养烹饪");
        categoryMap.put("psychology", "心理疏导");
        categoryMap.put("care", "护理技巧");
        video.setCategoryText(categoryMap.getOrDefault(video.getCategory(), video.getCategory()));

        if (video.getStatus() == 1) {
            video.setStatusText("启用");
        } else {
            video.setStatusText("禁用");
        }
    }
}