package com.care.module.trade.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.care.module.trade.entity.ProductOrder;
import com.care.module.trade.entity.ProductOrderDetailVO;
import com.care.module.trade.entity.ProductOrderItem;
import com.care.module.trade.mapper.ProductOrderItemMapper;
import com.care.module.trade.mapper.ProductOrderMapper;
import com.care.module.user.entity.AppUser;
import com.care.module.user.mapper.AppUserMapper;
import com.care.module.user.service.AppMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ProductOrderService extends ServiceImpl<ProductOrderMapper, ProductOrder> {

    @Autowired
    private ProductOrderItemMapper productOrderItemMapper;
    @Autowired
    private AppUserMapper appUserMapper;
    @Autowired
    private AppMessageService appMessageService;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public IPage<ProductOrder> getPage(Integer pageNum, Integer pageSize, String orderNo, Integer status,
                                       String keyword, String startDate, String endDate) {
        Page<ProductOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ProductOrder> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(orderNo)) {
            wrapper.like(ProductOrder::getOrderNo, orderNo);
        }
        if (status != null) {
            wrapper.eq(ProductOrder::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(ProductOrder::getElderName, keyword)
                    .or().like(ProductOrder::getContactPhone, keyword)
                    .or().like(ProductOrder::getProductName, keyword));
        }
        if (StringUtils.hasText(startDate)) {
            wrapper.ge(ProductOrder::getCreateTime, startDate + " 00:00:00");
        }
        if (StringUtils.hasText(endDate)) {
            wrapper.le(ProductOrder::getCreateTime, endDate + " 23:59:59");
        }
        wrapper.orderByDesc(ProductOrder::getCreateTime);
        return page(page, wrapper);
    }

    private List<ProductOrderItem> listItems(Long orderId) {
        LambdaQueryWrapper<ProductOrderItem> w = new LambdaQueryWrapper<>();
        w.eq(ProductOrderItem::getOrderId, orderId).orderByAsc(ProductOrderItem::getId);
        return productOrderItemMapper.selectList(w);
    }

    /** 装配订单详情视图 */
    public ProductOrderDetailVO getDetail(Long id) {
        ProductOrder order = getById(id);
        if (order == null) {
            return null;
        }
        ProductOrderDetailVO vo = new ProductOrderDetailVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setStatus(order.getStatus());
        vo.setOrderSource(order.getOrderSource());
        vo.setRemark(order.getRemark());
        vo.setCreateTime(order.getCreateTime() == null ? null : order.getCreateTime().format(DTF));

        // 商品明细
        List<ProductOrderItem> items = listItems(id);
        vo.setItems(items);
        if (!items.isEmpty()) {
            ProductOrderItem first = items.get(0);
            ProductOrderDetailVO.ProductInfo p = new ProductOrderDetailVO.ProductInfo();
            p.setId(first.getProductId());
            p.setName(items.size() > 1 ? first.getProductName() + " 等" + items.size() + "件商品" : first.getProductName());
            p.setPrice(first.getPrice());
            p.setPicUrl(order.getProductImage());
            vo.setProduct(p);
            vo.setQuantity(order.getProductCount());
            BigDecimal subtotalSum = items.stream()
                    .map(i -> i.getSubtotal() == null ? BigDecimal.ZERO : i.getSubtotal())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            vo.setSubtotal(subtotalSum);
        }

        // 费用明细
        vo.setTotalAmount(order.getGoodsAmount());
        vo.setDiscountAmount(order.getDiscountPrice());
        vo.setPayableAmount(order.getPayablePrice());
        vo.setActualAmount(order.getTotalPrice());
        vo.setPayMethod(order.getPayType());

        // 用户信息
        if (order.getUserId() != null) {
            AppUser user = appUserMapper.selectById(order.getUserId());
            if (user != null) {
                vo.setUserId(user.getId());
                vo.setUserName(user.getUsername());   // 昵称
                vo.setUserPhone(user.getPhone());
            }
        }
        if (vo.getUserName() == null) {
            vo.setUserName(order.getElderName());
            vo.setUserPhone(order.getContactPhone());
        }

        // 收货信息
        vo.setAppointmentAddress(order.getAddress());
        vo.setAppointmentTime(order.getAppointmentTime() == null ? null : order.getAppointmentTime().format(DTF));

        // 派单/工单信息
        vo.setWorkerName(order.getWorkerName());
        vo.setDispatchUser(order.getDispatchUser());
        vo.setDispatchTime(order.getDispatchTime() == null ? null : order.getDispatchTime().format(DTF));
        vo.setServiceOrderNo(order.getServiceOrderNo());

        // 操作日志（依据关键时间点生成）
        String ct = vo.getCreateTime();
        if (ct != null) {
            vo.getLogs().add(new ProductOrderDetailVO.LogItem(ct, "用户提交订单"));
        }
        if (order.getPayTime() != null) {
            vo.getLogs().add(new ProductOrderDetailVO.LogItem(order.getPayTime().format(DTF),
                    "订单支付成功（" + (order.getPayType() == null ? "-" : order.getPayType()) + "）"));
        }
        Integer st = order.getStatus();
        String ut = order.getUpdateTime() == null ? "" : order.getUpdateTime().format(DTF);
        if (st != null) {
            switch (st) {
                case 2: vo.getLogs().add(new ProductOrderDetailVO.LogItem(ut, "订单已支付，待接单")); break;
                case 3: vo.getLogs().add(new ProductOrderDetailVO.LogItem(ut, "已派单，服务人员：" + (order.getWorkerName() == null ? "-" : order.getWorkerName()))); break;
                case 4: vo.getLogs().add(new ProductOrderDetailVO.LogItem(ut, "订单已完成")); break;
                case 5: vo.getLogs().add(new ProductOrderDetailVO.LogItem(ut, "订单已关闭")); break;
                case 6: vo.getLogs().add(new ProductOrderDetailVO.LogItem(ut, "发起退款/售后")); break;
                default: break;
            }
        }
        return vo;
    }

    public void add(ProductOrder order) {
        order.setOrderNo("PO" + System.currentTimeMillis());
        save(order);
    }

    public void updateStatus(Long id, Integer status) {
        ProductOrder order = getById(id);
        if (order == null) throw new RuntimeException("订单不存在");
        order.setStatus(status);
        updateById(order);
    }

    /** 修改价格（改价：调整应付/实付 + 备注） */
    public void updatePrice(Long id, BigDecimal price, String remark) {
        ProductOrder order = getById(id);
        if (order == null) throw new RuntimeException("订单不存在");
        if (price != null) {
            order.setPayablePrice(price);
            order.setTotalPrice(price);
        }
        if (StringUtils.hasText(remark)) {
            order.setRemark(remark);
        }
        updateById(order);
    }

    /** 关闭订单 */
    public void close(Long id) {
        ProductOrder order = getById(id);
        if (order == null) throw new RuntimeException("订单不存在");
        order.setStatus(5);
        updateById(order);
    }

    /**
     * 模拟支付：将待付款(1)的订单完成支付，流转为已支付待接单(2)，
     * 写入支付方式与支付时间，并向用户推送支付成功通知。
     */
    @Transactional(rollbackFor = Exception.class)
    public void pay(Long orderId, Long userId, String payType) {
        ProductOrder order = getById(orderId);
        if (order == null) throw new RuntimeException("订单不存在");
        if (order.getUserId() == null || !order.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作该订单");
        }
        if (order.getStatus() == null || order.getStatus() != 1) {
            throw new RuntimeException("当前订单状态不可支付");
        }
        String type = (payType == null || payType.trim().isEmpty()) ? "余额支付" : payType.trim();
        order.setPayType(type);
        order.setPayTime(LocalDateTime.now());
        order.setStatus(2); // 已支付，待接单
        updateById(order);

        // 支付成功通知
        try {
            String name = order.getProductName() == null ? "商品" : order.getProductName();
            appMessageService.sendToUser(userId, "支付成功",
                    "您购买的商品【" + name + "】已支付成功，订单号：" + order.getOrderNo()
                            + "，支付方式：" + type + "。", "系统通知");
        } catch (Exception ignored) {
            // 通知失败不影响支付结果
        }
    }

    /**
     * 手动派单：将待接单(2)的订单指派服务人员，并流转为待服务(3)。
     * 同时生成关联工单编号，记录派单人与派单时间。
     */
    public void dispatch(Long id, Long workerId, String workerName, LocalDateTime appointmentTime, String operator) {
        ProductOrder order = getById(id);
        if (order == null) throw new RuntimeException("订单不存在");
        if (order.getStatus() != 2) throw new RuntimeException("只有待接单状态的订单可以派单");
        order.setWorkerId(workerId);
        order.setWorkerName(workerName);
        order.setAppointmentTime(appointmentTime);
        order.setDispatchTime(LocalDateTime.now());
        order.setDispatchUser(operator);
        order.setServiceOrderNo("SO" + System.currentTimeMillis());
        order.setStatus(3); // 待服务
        updateById(order);
    }
}
