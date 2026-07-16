package com.care.module.trade.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品订单详情视图对象，供后台订单详情页使用。
 */
@Data
public class ProductOrderDetailVO implements Serializable {
    private Long id;
    private String orderNo;
    private Integer status;          // 1待付款 2待接单 3待服务 4已完成 5已关闭 6退款售后
    private String orderSource;      // 订单来源
    private String createTime;
    private String remark;

    // 商品信息（首商品，用于单商品展示）
    private ProductInfo product;
    private Integer quantity;
    private BigDecimal subtotal;
    private List<ProductOrderItem> items = new ArrayList<>();

    // 费用明细
    private BigDecimal totalAmount;   // 商品总价
    private BigDecimal discountAmount;// 优惠金额
    private BigDecimal payableAmount; // 应付款
    private BigDecimal actualAmount;  // 实付款
    private String payMethod;         // 支付方式

    // 用户信息
    private Long userId;
    private String userName;
    private String userPhone;

    // 预约/收货信息
    private String appointmentAddress;
    private String appointmentTime;
    private String estimatedDuration;

    // 派单/工单信息
    private String workerName;        // 派单服务人员
    private String dispatchUser;      // 派单人
    private String dispatchTime;      // 派单时间
    private String serviceOrderNo;    // 关联工单编号

    // 操作日志
    private List<LogItem> logs = new ArrayList<>();

    @Data
    public static class ProductInfo implements Serializable {
        private Long id;
        private String name;
        private String picUrl;
        private BigDecimal price;
    }

    @Data
    public static class LogItem implements Serializable {
        private String time;
        private String content;
        public LogItem() {}
        public LogItem(String time, String content) { this.time = time; this.content = content; }
    }
}
