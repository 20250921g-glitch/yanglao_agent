package com.care.module.service.worker.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.service.worker.entity.ServiceWorkerTagRelation;
import com.care.module.service.worker.mapper.ServiceWorkerTagRelationMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServiceWorkerTagRelationService extends ServiceImpl<ServiceWorkerTagRelationMapper, ServiceWorkerTagRelation> {

    public List<Long> getTagIdsByWorkerId(Long workerId) {
        LambdaQueryWrapper<ServiceWorkerTagRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceWorkerTagRelation::getWorkerId, workerId);
        return list(wrapper).stream()
                .map(ServiceWorkerTagRelation::getTagId)
                .collect(Collectors.toList());
    }

    public void saveWorkerTags(Long workerId, List<Long> tagIds) {
        // 删除旧关系
        LambdaQueryWrapper<ServiceWorkerTagRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceWorkerTagRelation::getWorkerId, workerId);
        remove(wrapper);
        // 插入新关系
        if (tagIds != null && !tagIds.isEmpty()) {
            List<ServiceWorkerTagRelation> relations = tagIds.stream()
                    .map(tagId -> {
                        ServiceWorkerTagRelation r = new ServiceWorkerTagRelation();
                        r.setWorkerId(workerId);
                        r.setTagId(tagId);
                        return r;
                    })
                    .collect(Collectors.toList());
            saveBatch(relations);
        }
    }

    public void deleteByWorkerId(Long workerId) {
        LambdaQueryWrapper<ServiceWorkerTagRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceWorkerTagRelation::getWorkerId, workerId);
        remove(wrapper);
    }
}
