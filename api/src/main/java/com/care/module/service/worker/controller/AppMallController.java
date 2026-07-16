package com.care.module.service.worker.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.care.common.cache.CacheHelper;
import com.care.common.cache.CacheTtl;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.health.entity.Elder;
import com.care.module.health.service.ElderService;
import com.care.module.product.entity.Product;
import com.care.module.product.service.ProductService;
import com.care.module.trade.entity.ProductOrder;
import com.care.module.trade.entity.ProductOrderItem;
import com.care.module.trade.mapper.ProductOrderItemMapper;
import com.care.module.trade.service.ProductOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * C 端普通用户 —— 养老商城。
 * - 商品为公开目录（仅上架 status=1）；
 * - 下单/我的订单/取消 均按 ProductOrder.user_id 做归属校验，
 *   与管理员后台 /api/trade/product-order（可看全部）完全隔离。
 */
@RestController
@RequestMapping("/api/app/mall")
@Api(tags = "C端用户-养老商城")
public class AppMallController {

    @Autowired
    private ProductService productService;
    @Autowired
    private ProductOrderService productOrderService;
    @Autowired
    private ProductOrderItemMapper productOrderItemMapper;
    @Autowired
    private ElderService elderService;
    @Autowired
    private CacheHelper cacheHelper;

    private Long currentUserId(HttpServletRequest request) {
        Object v = request.getAttribute("userId");
        return v == null ? null : (Long) v;
    }

    @ApiOperation("商品列表（仅上架）")
    @GetMapping("/products")
    public Result<List<Product>> products(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId) {
        String listKey = "product:list:" + (categoryId == null ? "all" : categoryId)
                + ":" + (keyword == null || keyword.trim().isEmpty() ? "all" : keyword);
        Object cached = cacheHelper.get(listKey);
        if (cached instanceof List) {
            @SuppressWarnings("unchecked")
            List<Product> list = (List<Product>) cached;
            return Result.success(list);
        }
        LambdaQueryWrapper<Product> w = new LambdaQueryWrapper<>();
        w.eq(Product::getStatus, 1);
        if (keyword != null && !keyword.trim().isEmpty()) {
            w.like(Product::getName, keyword);
        }
        if (categoryId != null) {
            w.eq(Product::getCategoryId, categoryId);
        }
        w.orderByDesc(Product::getRecommend).orderByAsc(Product::getSort).orderByDesc(Product::getCreateTime);
        List<Product> list = productService.list(w);
        cacheHelper.put(listKey, list, CacheTtl.PRODUCT_DETAIL);
        return Result.success(list);
    }

    @ApiOperation("商品详情")
    @GetMapping("/product/{id}")
    public Result<Product> productDetail(@PathVariable Long id) {
        String key = "product:" + id;
        Object cached = cacheHelper.get(key);
        if (cached instanceof Product) {
            Product p = (Product) cached;
            if (p.getStatus() != null && p.getStatus() == 1) {
                return Result.success(p);
            }
            return Result.error("商品不存在或已下架");
        }
        Product p = productService.getById(id);
        if (p == null || p.getStatus() == null || p.getStatus() != 1) {
            return Result.error("商品不存在或已下架");
        }
        cacheHelper.put(key, p, CacheTtl.PRODUCT_DETAIL);
        return Result.success(p);
    }

    @ApiOperation("我关联的老人（下单收货人可选）")
    @GetMapping("/elders")
    public Result<List<Elder>> elders(HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        List<Elder> list = elderService.lambdaQuery()
                .eq(Elder::getAppUserId, userId)
                .orderByDesc(Elder::getId)
                .list();
        return Result.success(list);
    }

    @ApiOperation("下单购买")
    @PostMapping("/order")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> order(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        if (body.get("productId") == null) {
            return Result.error("请选择商品");
        }
        Long productId = Long.valueOf(String.valueOf(body.get("productId")));
        int quantity = 1;
        if (body.get("quantity") != null && !String.valueOf(body.get("quantity")).trim().isEmpty()) {
            quantity = Integer.parseInt(String.valueOf(body.get("quantity")));
        }
        if (quantity < 1) {
            return Result.error("购买数量必须大于 0");
        }

        Product p = productService.getById(productId);
        if (p == null || p.getStatus() == null || p.getStatus() != 1) {
            return Result.error("商品不存在或已下架");
        }

        // 收货老人（可选），若填写则做归属校验
        Long elderId = null;
        String elderName = null;
        if (body.get("elderId") != null && !String.valueOf(body.get("elderId")).trim().isEmpty()) {
            elderId = Long.valueOf(String.valueOf(body.get("elderId")));
            Elder elder = elderService.getById(elderId);
            if (elder == null || elder.getAppUserId() == null || !elder.getAppUserId().equals(userId)) {
                return Result.forbidden("无权为该老人下单");
            }
            elderName = elder.getName();
        }

        // 原子扣减库存：仅当库存足够时才扣减成功，避免并发超卖。
        // 只有当商品设置了库存字段（非 null）时才做扣减；null 表示不做库存管理。
        if (p.getStock() != null) {
            boolean deducted = productService.deductStock(productId, quantity);
            if (!deducted) {
                Product latest = productService.getById(productId);
                Integer left = latest != null && latest.getStock() != null ? latest.getStock() : 0;
                return Result.error("库存不足，当前仅剩 " + left);
            }
        }

        BigDecimal price = p.getPrice() == null ? BigDecimal.ZERO : p.getPrice();
        BigDecimal goods = price.multiply(new BigDecimal(quantity));

        ProductOrder o = new ProductOrder();
        o.setUserId(userId);
        o.setElderId(elderId);
        o.setElderName(elderName);
        o.setAddress((String) body.get("address"));
        o.setContactName((String) body.get("contactName"));
        o.setContactPhone((String) body.get("contactPhone"));
        o.setProductName(p.getName());
        o.setProductImage(p.getImage());
        o.setProductCount(quantity);
        o.setGoodsAmount(goods);
        o.setDiscountPrice(BigDecimal.ZERO);
        o.setFreight(BigDecimal.ZERO);
        o.setPayablePrice(goods);
        o.setTotalPrice(goods);
        o.setOrderSource("APP");
        o.setRemark((String) body.get("remark"));
        o.setStatus(1); // 待付款
        productOrderService.add(o); // 内部生成 orderNo 并 save，回填 id

        // 保存订单明细
        ProductOrderItem item = new ProductOrderItem();
        item.setOrderId(o.getId());
        item.setProductId(productId);
        item.setProductName(p.getName());
        item.setPrice(price);
        item.setQuantity(quantity);
        item.setSubtotal(goods);
        productOrderItemMapper.insert(item);

        return Result.ok("下单成功，请及时付款");
    }

    @ApiOperation("我的商城订单")
    @GetMapping("/my-orders")
    public Result<PageResult<ProductOrder>> myOrders(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        Page<ProductOrder> page = new Page<>(pageNum, pageSize);
        page.setSearchCount(false);
        LambdaQueryWrapper<ProductOrder> w = new LambdaQueryWrapper<>();
        w.eq(ProductOrder::getUserId, userId);
        if (status != null) {
            w.eq(ProductOrder::getStatus, status);
        }
        w.orderByDesc(ProductOrder::getCreateTime);
        Page<ProductOrder> res = productOrderService.page(page, w);
        long total = productOrderService.count(w);
        return Result.success(new PageResult<>(total, res.getRecords()));
    }

    @ApiOperation("取消订单（仅待付款/待接单可取消）")
    @PostMapping("/cancel/{orderId}")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> cancel(@PathVariable Long orderId, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        ProductOrder o = productOrderService.getById(orderId);
        if (o == null) {
            return Result.error("订单不存在");
        }
        if (o.getUserId() == null || !o.getUserId().equals(userId)) {
            return Result.forbidden("无权操作该订单");
        }
        if (o.getStatus() == null || (o.getStatus() != 1 && o.getStatus() != 2)) {
            return Result.error("当前订单状态不可取消");
        }
        o.setStatus(5); // 已关闭
        productOrderService.updateById(o);

        // 回补库存
        List<ProductOrderItem> items = productOrderItemMapper.selectList(
                new LambdaQueryWrapper<ProductOrderItem>().eq(ProductOrderItem::getOrderId, orderId));
        for (ProductOrderItem it : items) {
            if (it.getProductId() != null && it.getQuantity() != null) {
                try {
                    productService.restoreStock(it.getProductId(), it.getQuantity());
                } catch (Exception ignored) {
                }
            }
        }
        return Result.ok("已取消");
    }
}
