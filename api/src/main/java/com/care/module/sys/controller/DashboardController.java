package com.care.module.sys.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.care.common.result.Result;
import com.care.module.health.entity.Elder;
import com.care.module.health.mapper.ElderMapper;
import com.care.module.health.entity.HealthRecord;
import com.care.module.health.mapper.HealthRecordMapper;
import com.care.module.product.entity.Product;
import com.care.module.product.mapper.ProductMapper;
import com.care.module.product.mapper.ProductCategoryMapper;
import com.care.module.product.entity.ProductCategory;
import com.care.module.service.worker.entity.ServiceWorker;
import com.care.module.service.worker.mapper.ServiceWorkerMapper;
import com.care.module.trade.entity.ProductOrder;
import com.care.module.trade.entity.ProductOrderItem;
import com.care.module.trade.entity.ServiceOrder;
import com.care.module.trade.mapper.ProductOrderMapper;
import com.care.module.trade.mapper.ProductOrderItemMapper;
import com.care.module.trade.mapper.ServiceOrderMapper;
import com.care.module.user.entity.AppUser;
import com.care.module.user.entity.UserTag;
import com.care.module.user.entity.UserTagRelation;
import com.care.module.user.mapper.AppUserMapper;
import com.care.module.user.mapper.UserTagMapper;
import com.care.module.user.mapper.UserTagRelationMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Api(tags = "工作台-数据统计")
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private ElderMapper elderMapper;
    @Autowired
    private HealthRecordMapper healthRecordMapper;
    @Autowired
    private ServiceOrderMapper serviceOrderMapper;
    @Autowired
    private ProductOrderMapper productOrderMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ProductCategoryMapper productCategoryMapper;
    @Autowired
    private ProductOrderItemMapper productOrderItemMapper;
    @Autowired
    private ServiceWorkerMapper serviceWorkerMapper;
    @Autowired
    private AppUserMapper appUserMapper;
    @Autowired
    private UserTagMapper userTagMapper;
    @Autowired
    private UserTagRelationMapper userTagRelationMapper;

    @ApiOperation("获取工作台统计数据")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("elderCount", elderMapper.selectCount(null));
        stats.put("healthRecordCount", healthRecordMapper.selectCount(null));
        stats.put("serviceOrderCount", serviceOrderMapper.selectCount(null));
        stats.put("productOrderCount", productOrderMapper.selectCount(null));
        stats.put("productCount", productMapper.selectCount(null));
        stats.put("serviceWorkerCount", serviceWorkerMapper.selectCount(null));
        stats.put("appUserCount", appUserMapper.selectCount(null));
        return Result.success(stats);
    }

    @ApiOperation("获取顶部统计卡片数据")
    @GetMapping("/stat-cards")
    public Result<List<Map<String, Object>>> getStatCards() {
        List<Map<String, Object>> cards = new ArrayList<>();
        
        // 计算本周和上周的数据
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6);
        LocalDate lastWeekStart = today.minusDays(13);
        LocalDateTime thisWeekBegin = weekStart.atStartOfDay();
        LocalDateTime thisWeekEnd = today.atTime(23, 59, 59);
        LocalDateTime lastWeekBegin = lastWeekStart.atStartOfDay();
        LocalDateTime lastWeekEnd = weekStart.minusDays(1).atTime(23, 59, 59);
        
        // 1. 新增用户数量
        Long thisWeekUsers = appUserMapper.selectCount(
                new QueryWrapper<AppUser>().ge("create_time", thisWeekBegin).le("create_time", thisWeekEnd));
        Long lastWeekUsers = appUserMapper.selectCount(
                new QueryWrapper<AppUser>().ge("create_time", lastWeekBegin).le("create_time", lastWeekEnd));
        cards.add(createCard("新增用户数量", thisWeekUsers.intValue(), 
                calculateChange(thisWeekUsers, lastWeekUsers), "User", "#409EFF", "较上周"));
        
        // 2. 新增工单数量
        Long thisWeekOrders = serviceOrderMapper.selectCount(
                new QueryWrapper<ServiceOrder>().ge("create_time", thisWeekBegin).le("create_time", thisWeekEnd));
        Long lastWeekOrders = serviceOrderMapper.selectCount(
                new QueryWrapper<ServiceOrder>().ge("create_time", lastWeekBegin).le("create_time", lastWeekEnd));
        cards.add(createCard("新增工单数量", thisWeekOrders.intValue(), 
                calculateChange(thisWeekOrders, lastWeekOrders), "Document", "#E6A23C", "较上周"));
        
        // 3. 新增订单数量
        Long thisWeekProductOrders = productOrderMapper.selectCount(
                new QueryWrapper<ProductOrder>().ge("create_time", thisWeekBegin).le("create_time", thisWeekEnd));
        Long lastWeekProductOrders = productOrderMapper.selectCount(
                new QueryWrapper<ProductOrder>().ge("create_time", lastWeekBegin).le("create_time", lastWeekEnd));
        cards.add(createCard("新增订单数量", thisWeekProductOrders.intValue(), 
                calculateChange(thisWeekProductOrders, lastWeekProductOrders), "ShoppingCart", "#67C23A", "较上周"));
        
        // 4. 新增动态数量 (用健康记录数代替)
        Long thisWeekRecords = healthRecordMapper.selectCount(
                new QueryWrapper<HealthRecord>().ge("create_time", thisWeekBegin).le("create_time", thisWeekEnd));
        Long yesterdayRecords = healthRecordMapper.selectCount(
                new QueryWrapper<HealthRecord>().ge("create_time", today.minusDays(1).atStartOfDay())
                    .le("create_time", today.minusDays(1).atTime(23, 59, 59)));
        cards.add(createCard("新增动态数量", thisWeekRecords.intValue(), 
                calculateChange(thisWeekRecords, yesterdayRecords), "Bell", "#F56C6C", "较昨日"));
        
        return Result.success(cards);
    }
    
    private Map<String, Object> createCard(String title, int value, int change, String icon, String color, String label) {
        Map<String, Object> card = new HashMap<>();
        card.put("title", title);
        card.put("value", value);
        card.put("change", change);
        card.put("icon", icon);
        card.put("color", color);
        card.put("label", label);
        card.put("trend", change >= 0 ? "up" : "down");
        return card;
    }
    
    private int calculateChange(Long current, Long previous) {
        if (previous == null || previous == 0) {
            return current > 0 ? 100 : 0;
        }
        return (int) Math.round((current - previous) * 100.0 / previous);
    }

    @ApiOperation("获取用户标签分布")
    @GetMapping("/tag-distribution")
    public Result<List<Map<String, Object>>> getTagDistribution() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        List<UserTag> tags = userTagMapper.selectList(
                new QueryWrapper<UserTag>().orderByDesc("id"));
        
        for (UserTag tag : tags) {
            Long count = userTagRelationMapper.selectCount(
                    new QueryWrapper<UserTagRelation>().eq("tag_id", tag.getId()));
            if (count > 0) {
                Map<String, Object> item = new HashMap<>();
                item.put("tagName", tag.getTagName());
                item.put("count", count);
                item.put("color", tag.getColor());
                result.add(item);
            }
        }
        
        return Result.success(result);
    }

    @ApiOperation("获取各服务类型商品订单量占比")
    @GetMapping("/service-type-distribution")
    public Result<List<Map<String, Object>>> getServiceTypeDistribution() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 通过product_order_item关联product表获取category_id
        List<Map<String, Object>> list = productOrderItemMapper.selectMaps(
                new QueryWrapper<ProductOrderItem>()
                        .select("product_id, SUM(quantity) AS total")
                        .isNotNull("product_id")
                        .groupBy("product_id")
        );
        
        // 按category_id汇总
        Map<Long, Long> categoryTotals = new HashMap<>();
        for (Map<String, Object> item : list) {
            Long productId = (Long) item.get("product_id");
            Long total = ((Number) item.get("total")).longValue();
            if (productId != null) {
                Product p = productMapper.selectById(productId);
                if (p != null && p.getCategoryId() != null) {
                    categoryTotals.merge(p.getCategoryId(), total, Long::sum);
                }
            }
        }
        
        // 映射category名称
        Map<Long, String> categoryNameMap = new HashMap<>();
        categoryNameMap.put(1L, "保健用品");
        categoryNameMap.put(2L, "康复器材");
        categoryNameMap.put(3L, "护理用品");
        categoryNameMap.put(4L, "营养食品");
        
        Map<String, String> colorMap = new HashMap<>();
        colorMap.put("保健用品", "#409EFF");
        colorMap.put("康复器材", "#67C23A");
        colorMap.put("护理用品", "#E6A23C");
        colorMap.put("营养食品", "#F56C6C");
        
        for (Map.Entry<Long, Long> entry : categoryTotals.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            String name = categoryNameMap.getOrDefault(entry.getKey(), "其他");
            item.put("name", name);
            item.put("value", entry.getValue());
            item.put("color", colorMap.getOrDefault(name, "#909399"));
            result.add(item);
        }
        
        // 如果没数据，返回示例数据
        if (result.isEmpty()) {
            Map<String, Object> item1 = new HashMap<>();
            item1.put("name", "保健用品");
            item1.put("value", 5L);
            item1.put("color", "#409EFF");
            result.add(item1);
            
            Map<String, Object> item2 = new HashMap<>();
            item2.put("name", "康复器材");
            item2.put("value", 1L);
            item2.put("color", "#67C23A");
            result.add(item2);
            
            Map<String, Object> item3 = new HashMap<>();
            item3.put("name", "护理用品");
            item3.put("value", 1L);
            item3.put("color", "#E6A23C");
            result.add(item3);
            
            Map<String, Object> item4 = new HashMap<>();
            item4.put("name", "营养食品");
            item4.put("value", 2L);
            item4.put("color", "#F56C6C");
            result.add(item4);
        }
        
        return Result.success(result);
    }

    @ApiOperation("获取最近7天用户新增趋势")
    @GetMapping("/user-trend")
    public Result<List<Map<String, Object>>> getUserTrend() {
        List<Map<String, Object>> result = new ArrayList<>();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM-dd");
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            String label = day.format(dtf);
            LocalDateTime startOfDay = day.atStartOfDay();
            LocalDateTime endOfDay = day.atTime(23, 59, 59);
            Long count = appUserMapper.selectCount(
                    new QueryWrapper<AppUser>()
                            .ge("create_time", startOfDay)
                            .le("create_time", endOfDay)
            );
            Map<String, Object> item = new HashMap<>();
            item.put("date", label);
            item.put("count", count);
            result.add(item);
        }
        return Result.success(result);
    }

    @ApiOperation("支付榜TOP5商品排行")
    @GetMapping("/product-top")
    public Result<List<Map<String, Object>>> getProductTop() {
        List<Map<String, Object>> list = productOrderItemMapper.selectMaps(
                new QueryWrapper<ProductOrderItem>()
                        .select("product_id, product_name, SUM(quantity) AS order_count")
                        .isNotNull("product_id")
                        .groupBy("product_id", "product_name")
                        .orderByDesc("order_count")
                        .last("LIMIT 5")
        );
        
        if (list != null && !list.isEmpty()) {
            for (Map<String, Object> item : list) {
                Long productId = (Long) item.get("product_id");
                if (productId != null) {
                    Product p = productMapper.selectById(productId);
                    if (p != null) {
                        item.put("product_image", p.getImage());
                    }
                }
            }
        }
        
        if (list == null || list.isEmpty()) {
            List<Product> products = productMapper.selectList(
                    new QueryWrapper<Product>().last("LIMIT 5"));
            list = new ArrayList<>();
            for (Product p : products) {
                Map<String, Object> item = new HashMap<>();
                item.put("product_id", p.getId());
                item.put("product_name", p.getName());
                item.put("product_image", p.getImage());
                item.put("order_count", 0);
                list.add(item);
            }
        }
        return Result.success(list);
    }

    @ApiOperation("服务人员业绩TOP5")
    @GetMapping("/worker-top")
    public Result<List<Map<String, Object>>> getWorkerTop() {
        List<Map<String, Object>> list = serviceOrderMapper.selectMaps(
                new QueryWrapper<ServiceOrder>()
                        .select("nurse_id, nurse_name, service_type, COUNT(*) AS order_count")
                        .isNotNull("nurse_id")
                        .groupBy("nurse_id", "nurse_name", "service_type")
                        .orderByDesc("order_count")
                        .last("LIMIT 5")
        );
        
        if (list != null && !list.isEmpty()) {
            for (Map<String, Object> item : list) {
                Long nurseId = (Long) item.get("nurse_id");
                if (nurseId != null) {
                    ServiceWorker w = serviceWorkerMapper.selectById(nurseId);
                    if (w != null) {
                        item.put("worker_avatar", w.getAvatar());
                    }
                }
            }
        }
        
        if (list == null || list.isEmpty()) {
            List<ServiceWorker> workers = serviceWorkerMapper.selectList(
                    new QueryWrapper<ServiceWorker>().last("LIMIT 5"));
            list = new ArrayList<>();
            for (ServiceWorker w : workers) {
                Map<String, Object> item = new HashMap<>();
                item.put("nurse_id", w.getId());
                item.put("nurse_name", w.getName());
                item.put("worker_avatar", w.getAvatar());
                item.put("service_type", w.getServiceType());
                item.put("order_count", 0);
                list.add(item);
            }
        }
        return Result.success(list);
    }
}
