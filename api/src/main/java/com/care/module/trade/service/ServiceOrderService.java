package com.care.module.trade.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.health.entity.Elder;
import com.care.module.health.service.ElderService;
import com.care.module.trade.entity.CommissionRecord;
import com.care.module.trade.entity.ServiceOrder;
import com.care.module.trade.mapper.CommissionRecordMapper;
import com.care.module.trade.mapper.ServiceOrderMapper;
import com.care.module.user.service.AppMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ServiceOrderService extends ServiceImpl<ServiceOrderMapper, ServiceOrder> {

    @Autowired
    private CommissionRecordMapper commissionRecordMapper;

    @Autowired
    private AppMessageService appMessageService;

    @Autowired
    private ElderService elderService;

    public IPage<ServiceOrder> getPage(Integer pageNum, Integer pageSize, String orderNo, Integer status, String elderName) {
        Page<ServiceOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ServiceOrder> wrapper = new LambdaQueryWrapper<>();
        if (orderNo != null && !orderNo.isEmpty()) {
            wrapper.eq(ServiceOrder::getOrderNo, orderNo);
        }
        if (status != null) {
            wrapper.eq(ServiceOrder::getStatus, status);
        }
        if (elderName != null && !elderName.isEmpty()) {
            wrapper.like(ServiceOrder::getElderName, elderName);
        }
        wrapper.orderByDesc(ServiceOrder::getCreateTime);
        return page(page, wrapper);
    }

    public void add(ServiceOrder order) {
        order.setOrderNo("SO" + System.currentTimeMillis());
        save(order);
    }

    public void updateStatus(Long id, Integer status) {
        ServiceOrder order = getById(id);
        if (order == null) throw new RuntimeException("订单不存在");
        order.setStatus(status);
        updateById(order);
    }

    public void cancel(Long id) {
        updateStatus(id, 5);
    }

    public void assignWorker(Long id, Long nurseId, String nurseName) {
        ServiceOrder order = getById(id);
        if (order == null) throw new RuntimeException("订单不存在");
        if (order.getStatus() != 1) throw new RuntimeException("只有待接单状态的订单可以派单");
        order.setNurseId(nurseId);
        order.setNurseName(nurseName);
        order.setStatus(2); // 已接单
        updateById(order);
        sendServiceMessage(order, "服务人员已接单",
                "您为老人【" + order.getElderName() + "】预约的【" + order.getServiceName() + "】已由服务人员【" + nurseName + "】接单，预约时间：" + formatTime(order.getAppointmentTime()) + "，请保持电话畅通。");
    }

    public void startService(Long id) {
        ServiceOrder order = getById(id);
        if (order == null) throw new RuntimeException("订单不存在");
        if (order.getStatus() != 2) throw new RuntimeException("只有已接单状态的订单可以开始服务");
        order.setStatus(3); // 服务中
        updateById(order);
        sendServiceMessage(order, "服务已开始",
                "您为老人【" + order.getElderName() + "】预约的【" + order.getServiceName() + "】已开始服务，服务人员：【" + order.getNurseName() + "】。");
    }

    public void completeService(Long id) {
        ServiceOrder order = getById(id);
        if (order == null) throw new RuntimeException("订单不存在");
        if (order.getStatus() != 3) throw new RuntimeException("只有服务中的订单可以完成");
        order.setStatus(4); // 已完成
        updateById(order);
        sendServiceMessage(order, "服务已完成",
                "您为老人【" + order.getElderName() + "】预约的【" + order.getServiceName() + "】已完成，服务人员：【" + order.getNurseName() + "】，欢迎评价。");
        calculateCommission(order);
    }

    private String formatTime(LocalDateTime t) {
        if (t == null) return "";
        return t.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    private void sendServiceMessage(ServiceOrder order, String title, String content) {
        try {
            Elder elder = elderService.getById(order.getElderId());
            if (elder != null && elder.getAppUserId() != null) {
                appMessageService.sendToUser(elder.getAppUserId(), title, content, "服务提醒");
            }
        } catch (Exception ignored) {
        }
    }

    private void calculateCommission(ServiceOrder order) {
        // 佣金计算逻辑：按服务价格的10%计算
        if (order.getPrice() != null && order.getNurseId() != null) {
            BigDecimal rate = new BigDecimal("0.1");
            BigDecimal commission = order.getPrice().multiply(rate).setScale(2, RoundingMode.HALF_UP);
            
            // 创建佣金记录
            CommissionRecord record = new CommissionRecord();
            record.setOrderId(order.getId());
            record.setOrderNo(order.getOrderNo());
            record.setWorkerId(order.getNurseId());
            record.setWorkerName(order.getNurseName());
            record.setOrderAmount(order.getPrice());
            record.setCommissionRate(new BigDecimal("10.00"));
            record.setCommissionAmount(commission);
            record.setStatus(0); // 待结算
            commissionRecordMapper.insert(record);
            
            System.out.println("计算佣金: " + commission + "元, 服务人员: " + order.getNurseName());
        }
    }
}
