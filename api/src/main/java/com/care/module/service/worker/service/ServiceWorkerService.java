package com.care.module.service.worker.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.service.worker.entity.*;
import com.care.module.service.worker.mapper.ServiceWorkerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ServiceWorkerService extends ServiceImpl<ServiceWorkerMapper, ServiceWorker> {

    @Autowired
    private ServiceWorkerTagRelationService tagRelationService;
    @Autowired
    private ServiceWorkerTagService tagService;

    public IPage<ServiceWorker> getPage(Integer pageNum, Integer pageSize, String name, String phone,
                                        String serviceType, String tags, Integer auditStatus,
                                        Integer status, String startDate, String endDate) {
        Page<ServiceWorker> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ServiceWorker> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(ServiceWorker::getName, name);
        }
        if (StringUtils.hasText(phone)) {
            wrapper.eq(ServiceWorker::getPhone, phone);
        }
        if (StringUtils.hasText(serviceType)) {
            wrapper.eq(ServiceWorker::getServiceType, serviceType);
        }
        if (auditStatus != null) {
            wrapper.eq(ServiceWorker::getAuditStatus, auditStatus);
        }
        if (status != null) {
            wrapper.eq(ServiceWorker::getStatus, status);
        }
        if (StringUtils.hasText(startDate)) {
            wrapper.ge(ServiceWorker::getCreateTime, LocalDate.parse(startDate).atStartOfDay());
        }
        if (StringUtils.hasText(endDate)) {
            wrapper.le(ServiceWorker::getCreateTime, LocalDate.parse(endDate).atTime(23, 59, 59));
        }
        wrapper.orderByDesc(ServiceWorker::getCreateTime);
        // 强制计算总数（MP 3.5.x bug: page.getTotal()可能为0）
        page.setSearchCount(true);
        IPage<ServiceWorker> result = page(page, wrapper);
        // MP的count可能为0，用baseMapper强制算一次
        if (result.getTotal() == 0) {
            long cnt = baseMapper.selectCount(wrapper);
            ((Page<ServiceWorker>)result).setTotal(cnt);
        }

        // 填充标签和文本字段
        for (ServiceWorker worker : result.getRecords()) {
            fillExtraFields(worker);
        }

        // 标签过滤（多标签逗号分隔，需在结果中过滤）
        if (StringUtils.hasText(tags)) {
            List<Long> tagIdList = Arrays.stream(tags.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            if (!tagIdList.isEmpty()) {
                List<ServiceWorker> filtered = result.getRecords().stream()
                        .filter(w -> {
                            List<Long> workerTagIds = w.getTags().stream()
                                    .map(ServiceWorkerTag::getId)
                                    .collect(Collectors.toList());
                            return workerTagIds.containsAll(tagIdList);
                        })
                        .collect(Collectors.toList());
                result.setRecords(filtered);
            }
        }

        return result;
    }

    public ServiceWorker getByIdWithTags(Long id) {
        ServiceWorker worker = getById(id);
        if (worker != null) {
            fillExtraFields(worker);
        }
        return worker;
    }

    private void fillExtraFields(ServiceWorker worker) {
        // 填充审核状态文本
        if (worker.getAuditStatus() != null) {
            switch (worker.getAuditStatus()) {
                case 0: worker.setAuditStatusText("待审核"); break;
                case 1: worker.setAuditStatusText("已通过"); break;
                case 2: worker.setAuditStatusText("已拒绝"); break;
                default: worker.setAuditStatusText("未知");
            }
        }
        // 填充状态文本
        if (worker.getStatus() != null) {
            worker.setStatusText(worker.getStatus() == 1 ? "正常" : "禁用");
        }
        // 填充服务类型文本
        if (StringUtils.hasText(worker.getServiceType())) {
            worker.setServiceTypeText(worker.getServiceType());
        }
        // 填充标签
        List<Long> tagIds = tagRelationService.getTagIdsByWorkerId(worker.getId());
        if (!tagIds.isEmpty()) {
            List<ServiceWorkerTag> tags = tagService.listByIds(tagIds);
            worker.setTags(tags);
        } else {
            worker.setTags(Collections.emptyList());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void add(ServiceWorker worker) {
        worker.setAuditStatus(0);
        worker.setStatus(1);
        save(worker);
        if (worker.getTags() != null && !worker.getTags().isEmpty()) {
            List<Long> tagIds = worker.getTags().stream()
                    .map(ServiceWorkerTag::getId)
                    .collect(Collectors.toList());
            tagRelationService.saveWorkerTags(worker.getId(), tagIds);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateWorker(ServiceWorker worker) {
        updateById(worker);
        if (worker.getTags() != null) {
            List<Long> tagIds = worker.getTags().stream()
                    .map(ServiceWorkerTag::getId)
                    .collect(Collectors.toList());
            tagRelationService.saveWorkerTags(worker.getId(), tagIds);
        }
    }

    public void updateStatus(Long id, Integer status) {
        ServiceWorker worker = new ServiceWorker();
        worker.setId(id);
        worker.setStatus(status);
        updateById(worker);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        tagRelationService.deleteByWorkerId(id);
        removeById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void audit(Long id, Integer status, String rejectReason) {
        ServiceWorker worker = new ServiceWorker();
        worker.setId(id);
        worker.setAuditStatus(status);
        if (status == 2) {
            worker.setAuditRemark(rejectReason);
        }
        updateById(worker);
    }

    public List<ServiceWorkerTag> getTagsByWorkerId(Long workerId) {
        List<Long> tagIds = tagRelationService.getTagIdsByWorkerId(workerId);
        if (tagIds.isEmpty()) {
            return Collections.emptyList();
        }
        return tagService.listByIds(tagIds);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateTags(Long workerId, List<Long> tagIds) {
        tagRelationService.saveWorkerTags(workerId, tagIds);
    }

    public List<ServiceWorker> getList() {
        LambdaQueryWrapper<ServiceWorker> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceWorker::getStatus, 1);
        wrapper.orderByDesc(ServiceWorker::getCreateTime);
        return list(wrapper);
    }
}
