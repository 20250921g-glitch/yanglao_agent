package com.care.module.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.user.entity.AppMessage;
import com.care.module.user.entity.CouponRecord;
import com.care.module.user.mapper.AppMessageMapper;
import com.care.module.user.mapper.CouponRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class AppMessageService extends ServiceImpl<AppMessageMapper, AppMessage> {

    @Autowired
    private CouponRecordMapper couponRecordMapper;

    public IPage<AppMessage> getPage(Integer pageNum, Integer pageSize, String title, String type, Integer status) {
        Page<AppMessage> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AppMessage> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(title)) {
            wrapper.like(AppMessage::getTitle, title);
        }
        if (StringUtils.hasText(type)) {
            wrapper.eq(AppMessage::getType, type);
        }
        if (status != null) {
            wrapper.eq(AppMessage::getStatus, status);
        }
        wrapper.orderByDesc(AppMessage::getCreateTime);
        return page(page, wrapper);
    }

    @Transactional
    public void send(AppMessage message) {
        save(message);
        // 如果是优惠券发放类型的消息，同时创建优惠券领取记录
        if ("优惠券发放".equals(message.getType()) && StringUtils.hasText(message.getContent())) {
            // content 中可存储 couponId,userId 等信息，格式: couponId:userId
            String[] parts = message.getContent().split(",");
            for (String part : parts) {
                String[] ids = part.trim().split(":");
                if (ids.length >= 2) {
                    try {
                        CouponRecord record = new CouponRecord();
                        record.setCouponId(Long.parseLong(ids[0].trim()));
                        record.setUserId(Long.parseLong(ids[1].trim()));
                        record.setStatus(0);
                        record.setReceiveTime(LocalDateTime.now());
                        couponRecordMapper.insert(record);
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }

    public void delete(Long id) {
        removeById(id);
    }

    /**
     * 便捷方法：给指定用户发送一条系统通知。
     * target 设为该用户 userId，配合 C 端消息查询的 target 过滤，
     * 保证该消息仅对目标用户可见（不影响后台群发的广播消息）。
     */
    public void sendToUser(Long userId, String title, String content, String type) {
        AppMessage m = new AppMessage();
        m.setTitle(title);
        m.setContent(content);
        m.setType(type);
        m.setTarget(userId == null ? "all" : String.valueOf(userId));
        m.setStatus(1);
        m.setSendTime(LocalDateTime.now());
        save(m);
    }
}
