package com.care.module.service.worker.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.care.common.cache.CacheHelper;
import com.care.common.cache.CacheTtl;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.health.entity.Elder;
import com.care.module.health.service.ElderService;
import com.care.module.product.entity.ServiceProject;
import com.care.module.product.service.ServiceProjectService;
import com.care.module.trade.entity.ServiceOrder;
import com.care.module.trade.service.ServiceOrderService;
import com.care.module.user.service.AppMessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * C 端普通用户 —— 养老服务。
 * - 服务项目为公开目录（仅上架）；
 * - 下单/我的订单/取消 均按 elder.app_user_id 做归属校验，
 *   与管理员后台 /api/trade/service-order（可看全部）完全隔离。
 */
@RestController
@RequestMapping("/api/app/service")
@Api(tags = "C端用户-养老服务")
public class AppServiceController {

    @Autowired
    private ServiceProjectService serviceProjectService;
    @Autowired
    private ServiceOrderService serviceOrderService;
    @Autowired
    private ElderService elderService;
    @Autowired
    private CacheHelper cacheHelper;
    @Autowired
    private AppMessageService appMessageService;

    private Long currentUserId(HttpServletRequest request) {
        Object v = request.getAttribute("userId");
        return v == null ? null : (Long) v;
    }

    /** 当前用户名下的老人ID列表 */
    private List<Long> myElderIds(Long userId) {
        List<Elder> elders = elderService.lambdaQuery()
                .eq(Elder::getAppUserId, userId)
                .list();
        List<Long> ids = new ArrayList<>();
        for (Elder e : elders) {
            ids.add(e.getId());
        }
        return ids;
    }

    @ApiOperation("服务项目列表（仅上架）")
    @GetMapping("/projects")
    public Result<List<ServiceProject>> projects(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        String listKey = "service:project:list:" + (category == null || category.trim().isEmpty() ? "all" : category)
                + ":" + (keyword == null || keyword.trim().isEmpty() ? "all" : keyword);
        Object cached = cacheHelper.get(listKey);
        if (cached instanceof List) {
            @SuppressWarnings("unchecked")
            List<ServiceProject> list = (List<ServiceProject>) cached;
            return Result.success(list);
        }
        LambdaQueryWrapper<ServiceProject> w = new LambdaQueryWrapper<>();
        w.eq(ServiceProject::getStatus, 1);
        if (keyword != null && !keyword.trim().isEmpty()) {
            w.like(ServiceProject::getName, keyword);
        }
        if (category != null && !category.trim().isEmpty()) {
            w.eq(ServiceProject::getCategory, category);
        }
        w.orderByDesc(ServiceProject::getCreateTime);
        List<ServiceProject> list = serviceProjectService.list(w);
        cacheHelper.put(listKey, list, CacheTtl.SERVICE_DETAIL);
        return Result.success(list);
    }

    @ApiOperation("服务项目详情")
    @GetMapping("/project/{id}")
    public Result<ServiceProject> projectDetail(@PathVariable Long id) {
        String key = "service:project:" + id;
        Object cached = cacheHelper.get(key);
        if (cached instanceof ServiceProject) {
            ServiceProject p = (ServiceProject) cached;
            if (p.getStatus() != null && p.getStatus() == 1) {
                return Result.success(p);
            }
            return Result.notFound("服务项目不存在或已下架");
        }
        ServiceProject p = serviceProjectService.getById(id);
        if (p == null || p.getStatus() == null || p.getStatus() != 1) {
            return Result.notFound("服务项目不存在或已下架");
        }
        cacheHelper.put(key, p, CacheTtl.SERVICE_DETAIL);
        return Result.success(p);
    }

    @ApiOperation("我关联的老人（下单选择用）")
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

    @ApiOperation("预约下单")
    @PostMapping("/order")
    public Result<Void> order(@RequestBody java.util.Map<String, Object> body, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        if (body.get("projectId") == null || body.get("elderId") == null) {
            return Result.error("请选择服务项目和老人");
        }
        Long projectId = Long.valueOf(String.valueOf(body.get("projectId")));
        Long elderId = Long.valueOf(String.valueOf(body.get("elderId")));

        // 归属校验：老人必须属于当前用户
        Elder elder = elderService.getById(elderId);
        if (elder == null || elder.getAppUserId() == null || !elder.getAppUserId().equals(userId)) {
            return Result.forbidden("无权为该老人下单");
        }
        ServiceProject p = serviceProjectService.getById(projectId);
        if (p == null || p.getStatus() == null || p.getStatus() != 1) {
            return Result.notFound("服务项目不存在或已下架");
        }

        ServiceOrder o = new ServiceOrder();
        o.setElderId(elderId);
        o.setElderName(elder.getName());
        o.setServiceType(p.getCategory());
        o.setServiceName(p.getName());
        o.setPrice(p.getPrice());
        o.setStatus(1); // 待接单
        o.setRemark((String) body.get("remark"));
        Object at = body.get("appointmentTime");
        if (at != null && !String.valueOf(at).trim().isEmpty()) {
            String s = String.valueOf(at).replace("T", " ");
            if (s.length() == 16) {
                s = s + ":00";
            }
            LocalDateTime appointmentTime = LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            if (appointmentTime.isBefore(LocalDateTime.now())) {
                return Result.error("预约时间不能早于当前时间");
            }
            o.setAppointmentTime(appointmentTime);
        } else {
            return Result.error("请选择预约时间");
        }
        serviceOrderService.add(o); // 内部生成 orderNo
        // 预约成功后自动给用户发送消息通知
        try {
            appMessageService.sendToUser(userId, "服务预约成功",
                    "您为老人【" + elder.getName() + "】预约的【" + p.getName() + "】已提交，等待服务人员接单。", "系统通知");
        } catch (Exception ignored) {
        }
        return Result.ok("预约成功，等待服务人员接单");
    }

    @ApiOperation("我的服务订单")
    @GetMapping("/my-orders")
    public Result<PageResult<ServiceOrder>> myOrders(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        List<Long> elderIds = myElderIds(userId);
        if (elderIds.isEmpty()) {
            return Result.success(new PageResult<>(0L, new ArrayList<ServiceOrder>()));
        }
        Page<ServiceOrder> page = new Page<>(pageNum, pageSize);
        page.setSearchCount(false);
        LambdaQueryWrapper<ServiceOrder> w = new LambdaQueryWrapper<>();
        w.in(ServiceOrder::getElderId, elderIds);
        if (status != null) {
            w.eq(ServiceOrder::getStatus, status);
        }
        w.orderByDesc(ServiceOrder::getCreateTime);
        Page<ServiceOrder> res = serviceOrderService.page(page, w);
        long total = serviceOrderService.count(w);
        return Result.success(new PageResult<>(total, res.getRecords()));
    }

    @ApiOperation("取消服务订单（仅待接单/已接单可取消）")
    @PostMapping("/cancel/{orderId}")
    public Result<Void> cancel(@PathVariable Long orderId, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        ServiceOrder o = serviceOrderService.getById(orderId);
        if (o == null) {
            return Result.notFound("订单不存在");
        }
        List<Long> elderIds = myElderIds(userId);
        if (o.getElderId() == null || !elderIds.contains(o.getElderId())) {
            return Result.forbidden("无权操作该订单");
        }
        if (o.getStatus() == null || (o.getStatus() != 1 && o.getStatus() != 2)) {
            return Result.error("当前订单状态不可取消");
        }
        o.setStatus(5); // 已取消
        serviceOrderService.updateById(o);
        return Result.ok("已取消");
    }
}
