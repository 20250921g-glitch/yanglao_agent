package com.care.module.trade.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("product_order")
public class ProductOrder implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;          // 下单用户ID(app_user)
    private Long elderId;
    private String elderName;
    private String address;
    private String contactName;
    private String contactPhone;
    private BigDecimal totalPrice;      // 实付款
    private BigDecimal goodsAmount;     // 商品总价
    private BigDecimal discountPrice;   // 优惠金额
    private BigDecimal freight;         // 运费
    private BigDecimal payablePrice;    // 应付款
    private String payType;             // 支付方式
    private LocalDateTime payTime;      // 支付时间
    private String orderSource;         // 订单来源
    private String remark;              // 备注
    private String productName;         // 首商品名(冗余，列表展示)
    private String productImage;        // 首商品图(冗余，列表展示)
    private Integer productCount;       // 商品件数
    private Integer status;  // 1待付款 2待接单 3待服务 4已完成 5已关闭 6退款售后
    // 派单信息
    private Long workerId;            // 派单服务人员ID
    private String workerName;        // 派单服务人员姓名
    private LocalDateTime appointmentTime; // 预约上门时间
    private LocalDateTime dispatchTime;    // 派单时间
    private String dispatchUser;      // 派单人
    private String serviceOrderNo;    // 关联工单编号
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;

    // ===== 非数据库字段 =====
    @TableField(exist = false)
    private List<ProductOrderItem> items;   // 订单商品明细
}
