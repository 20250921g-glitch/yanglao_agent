package com.care.module.data.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.care.common.result.PageResult;
import com.care.common.result.Result;
import com.care.module.operation.entity.Dynamic;
import com.care.module.operation.mapper.DynamicMapper;
import com.care.module.product.entity.Product;
import com.care.module.product.mapper.ProductMapper;
import com.care.module.service.worker.entity.ServiceWorker;
import com.care.module.service.worker.mapper.ServiceWorkerMapper;
import com.care.module.trade.entity.*;
import com.care.module.trade.mapper.*;
import com.care.module.user.entity.AppUser;
import com.care.module.user.mapper.AppUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/data")
public class DataAnalysisController {

    @Autowired
    private AppUserMapper appUserMapper;
    @Autowired
    private ProductOrderMapper productOrderMapper;
    @Autowired
    private ProductOrderItemMapper productOrderItemMapper;
    @Autowired
    private ServiceOrderMapper serviceOrderMapper;
    @Autowired
    private ServiceWorkerMapper serviceWorkerMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private OrderEvaluationMapper orderEvaluationMapper;
    @Autowired
    private CommissionRecordMapper commissionRecordMapper;
    @Autowired
    private DynamicMapper dynamicMapper;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int COMPLETED_SERVICE_STATUS = 4;

    private static Number num(Object o) {
        if (o == null) return 0;
        return (Number) o;
    }

    private static long lng(Object o) {
        return o == null ? 0L : ((Number) o).longValue();
    }

    private static double dbl(Object o) {
        return o == null ? 0.0 : ((Number) o).doubleValue();
    }

    private static String fmtDate(Object o) {
        if (o == null) return null;
        if (o instanceof Date) return ((Date) o).toLocalDate().format(DATE_FMT);
        if (o instanceof LocalDate) return ((LocalDate) o).format(DATE_FMT);
        return o.toString();
    }

    // ============ 用户分析：近7天新增/活跃/复购 ============
    @GetMapping("/user-analysis/page")
    public Result<PageResult<Map<String, Object>>> getUserAnalysis() {
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> nu = appUserMapper.selectMaps(
                new QueryWrapper<AppUser>().select("DATE(create_time) AS d, COUNT(*) AS c").groupBy("DATE(create_time)"));
        Map<String, Long> newUsers = new HashMap<>();
        for (Map<String, Object> m : nu) newUsers.put(fmtDate(m.get("d")), lng(m.get("c")));

        List<Map<String, Object>> om = productOrderMapper.selectMaps(
                new QueryWrapper<ProductOrder>().select("user_id, DATE(create_time) AS d").isNotNull("user_id"));
        Map<String, Set<Long>> activeByDate = new HashMap<>();
        Map<Long, String> firstOrderDate = new HashMap<>();
        for (Map<String, Object> m : om) {
            Long uid = m.get("user_id") == null ? null : ((Number) m.get("user_id")).longValue();
            String d = fmtDate(m.get("d"));
            if (uid == null) continue;
            activeByDate.computeIfAbsent(d, k -> new HashSet<>()).add(uid);
            String f = firstOrderDate.get(uid);
            if (f == null || d.compareTo(f) < 0) firstOrderDate.put(uid, d);
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            String d = today.minusDays(i).format(DATE_FMT);
            long nuCount = newUsers.getOrDefault(d, 0L);
            Set<Long> active = activeByDate.getOrDefault(d, Collections.emptySet());
            long ru = active.stream()
                    .filter(u -> {
                        String f = firstOrderDate.get(u);
                        return f != null && f.compareTo(d) < 0;
                    }).count();
            Map<String, Object> item = new HashMap<>();
            item.put("date", d);
            item.put("newUsers", nuCount);
            item.put("activeUsers", (long) active.size());
            item.put("returnUsers", ru);
            list.add(item);
        }
        return Result.success(PageResult.of((long) list.size(), list));
    }

    // ============ 交易分析：近30天订单/金额/退款 ============
    @GetMapping("/trade-analysis/page")
    public Result<PageResult<Map<String, Object>>> getTradeAnalysis() {
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> all = productOrderMapper.selectMaps(
                new QueryWrapper<ProductOrder>().select("DATE(create_time) AS d, COUNT(*) AS orders, SUM(payable_price) AS amount")
                        .groupBy("DATE(create_time)"));
        Map<String, Map<String, Object>> allMap = new HashMap<>();
        for (Map<String, Object> m : all) allMap.put(fmtDate(m.get("d")), m);

        List<Map<String, Object>> rf = productOrderMapper.selectMaps(
                new QueryWrapper<ProductOrder>().select("DATE(create_time) AS d, COUNT(*) AS refunds, SUM(payable_price) AS refundAmount")
                        .eq("status", 6).groupBy("DATE(create_time)"));
        Map<String, Map<String, Object>> rfMap = new HashMap<>();
        for (Map<String, Object> m : rf) rfMap.put(fmtDate(m.get("d")), m);

        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            String d = today.minusDays(i).format(DATE_FMT);
            Map<String, Object> a = allMap.getOrDefault(d, Collections.emptyMap());
            Map<String, Object> r = rfMap.getOrDefault(d, Collections.emptyMap());
            Map<String, Object> item = new HashMap<>();
            item.put("date", d);
            item.put("orders", lng(a.get("orders")));
            item.put("amount", dbl(a.get("amount")));
            item.put("refunds", lng(r.get("refunds")));
            item.put("refundAmount", dbl(r.get("refundAmount")));
            list.add(item);
        }
        return Result.success(PageResult.of((long) list.size(), list));
    }

    // 支付方式占比（真实分布）
    @GetMapping("/trade-analysis/pay-method")
    public Result<List<Map<String, Object>>> getPayMethod() {
        List<Map<String, Object>> rows = productOrderMapper.selectMaps(
                new QueryWrapper<ProductOrder>().select("pay_type, COUNT(*) AS c").groupBy("pay_type"));
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> m : rows) {
            String pt = (String) m.get("pay_type");
            Map<String, Object> item = new HashMap<>();
            item.put("name", pt == null ? "未支付" : pt);
            item.put("value", lng(m.get("c")));
            list.add(item);
        }
        return Result.success(list);
    }

    // 交易时段分布（真实按小时聚合）
    @GetMapping("/trade-analysis/hourly")
    public Result<List<Map<String, Object>>> getHourly() {
        List<Map<String, Object>> rows = productOrderMapper.selectMaps(
                new QueryWrapper<ProductOrder>().select("HOUR(create_time) AS h, SUM(payable_price) AS amt, COUNT(*) AS c")
                        .groupBy("HOUR(create_time)"));
        Map<Integer, Map<String, Object>> map = new HashMap<>();
        for (Map<String, Object> m : rows) {
            Integer h = m.get("h") == null ? null : ((Number) m.get("h")).intValue();
            if (h == null) continue;
            Map<String, Object> it = new HashMap<>();
            it.put("amount", dbl(m.get("amt")));
            it.put("orders", lng(m.get("c")));
            map.put(h, it);
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (int h = 0; h < 24; h++) {
            Map<String, Object> it = map.getOrDefault(h, new HashMap<>());
            Map<String, Object> item = new HashMap<>();
            item.put("hour", String.format("%02d:00", h));
            item.put("amount", dbl(it.get("amount")));
            item.put("orders", lng(it.get("orders")));
            list.add(item);
        }
        return Result.success(list);
    }

    // ============ 服务分析：按服务类型 ============
    @GetMapping("/service-analysis/page")
    public Result<PageResult<Map<String, Object>>> getServiceAnalysis() {
        List<Map<String, Object>> rows = serviceOrderMapper.selectMaps(
                new QueryWrapper<ServiceOrder>().select("service_type, COUNT(*) AS orders, SUM(price) AS amount, " +
                        "SUM(CASE WHEN status=" + COMPLETED_SERVICE_STATUS + " THEN 1 ELSE 0 END) AS completed")
                        .groupBy("service_type"));

        // 评分：order_evaluation.order_no 关联 service_order.service_type
        List<Map<String, Object>> evals = orderEvaluationMapper.selectMaps(
                new QueryWrapper<OrderEvaluation>().select("order_no, score"));
        Map<String, List<Integer>> scoreByOrder = new HashMap<>();
        for (Map<String, Object> m : evals) {
            String on = (String) m.get("order_no");
            Integer s = m.get("score") == null ? null : ((Number) m.get("score")).intValue();
            if (on != null && s != null) scoreByOrder.computeIfAbsent(on, k -> new ArrayList<>()).add(s);
        }
        List<Map<String, Object>> soRows = serviceOrderMapper.selectMaps(
                new QueryWrapper<ServiceOrder>().select("order_no, service_type").isNotNull("order_no"));
        Map<String, String> orderType = new HashMap<>();
        for (Map<String, Object> m : soRows) orderType.put((String) m.get("order_no"), (String) m.get("service_type"));
        Map<String, List<Integer>> scoreByType = new HashMap<>();
        for (Map.Entry<String, List<Integer>> e : scoreByOrder.entrySet()) {
            String st = orderType.get(e.getKey());
            if (st != null) scoreByType.computeIfAbsent(st, k -> new ArrayList<>()).addAll(e.getValue());
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> m : rows) {
            String st = (String) m.get("service_type");
            if (st == null) st = "未分类";
            long orders = lng(m.get("orders"));
            double amount = dbl(m.get("amount"));
            long completed = lng(m.get("completed"));
            double completionRate = orders > 0 ? Math.round(completed * 1000.0 / orders) / 10.0 : 0;
            List<Integer> scores = scoreByType.getOrDefault(st, Collections.emptyList());
            double avgScore = scores.isEmpty() ? 0 : Math.round(
                    scores.stream().mapToInt(Integer::intValue).average().orElse(0) * 10) / 10.0;
            Map<String, Object> item = new HashMap<>();
            item.put("serviceType", st);
            item.put("orders", orders);
            item.put("amount", amount);
            item.put("completionRate", completionRate);
            item.put("avgScore", avgScore);
            list.add(item);
        }
        return Result.success(PageResult.of((long) list.size(), list));
    }

    // 服务人员业绩 TOP10（真实）
    @GetMapping("/service-analysis/worker-rank")
    public Result<List<Map<String, Object>>> getWorkerRank() {
        List<Map<String, Object>> rows = serviceOrderMapper.selectMaps(
                new QueryWrapper<ServiceOrder>().select("nurse_name, COUNT(*) AS c").isNotNull("nurse_id")
                        .groupBy("nurse_id", "nurse_name").orderByDesc("c").last("LIMIT 10"));
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> m : rows) {
            Map<String, Object> it = new HashMap<>();
            it.put("name", m.get("nurse_name"));
            it.put("value", lng(m.get("c")));
            list.add(it);
        }
        return Result.success(list);
    }

    // 服务区域分布（真实：service_order.nurse_id -> service_worker.region）
    @GetMapping("/service-analysis/area-rank")
    public Result<List<Map<String, Object>>> getAreaRank() {
        List<Map<String, Object>> soRows = serviceOrderMapper.selectMaps(
                new QueryWrapper<ServiceOrder>().select("nurse_id").isNotNull("nurse_id"));
        Set<Long> nurseIds = new HashSet<>();
        for (Map<String, Object> m : soRows) nurseIds.add(((Number) m.get("nurse_id")).longValue());
        if (nurseIds.isEmpty()) return Result.success(new ArrayList<>());

        List<Map<String, Object>> wk = serviceWorkerMapper.selectMaps(
                new QueryWrapper<ServiceWorker>().select("id, region").in("id", nurseIds));
        Map<Long, String> regionMap = new HashMap<>();
        for (Map<String, Object> m : wk) {
            if (m.get("region") != null) regionMap.put(((Number) m.get("id")).longValue(), (String) m.get("region"));
        }
        Map<String, Long> areaCount = new HashMap<>();
        for (Long nid : nurseIds) {
            String r = regionMap.get(nid);
            if (r != null) areaCount.merge(r, 1L, Long::sum);
        }
        List<Map<String, Object>> list = areaCount.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .map(e -> {
                    Map<String, Object> it = new HashMap<>();
                    it.put("name", e.getKey());
                    it.put("value", e.getValue());
                    return it;
                }).collect(Collectors.toList());
        return Result.success(list);
    }

    // ============ 商品分析 TOP10 ============
    @GetMapping("/product-analysis/page")
    public Result<PageResult<Map<String, Object>>> getProductAnalysis() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.minusDays(30).atStartOfDay();
        LocalDateTime prevStart = today.minusDays(60).atStartOfDay();
        LocalDateTime prevEnd = today.minusDays(30).atStartOfDay();

        List<Map<String, Object>> items = productOrderItemMapper.selectMaps(
                new QueryWrapper<ProductOrderItem>().select("product_id, product_name, SUM(quantity) AS sales, SUM(subtotal) AS amount")
                        .isNotNull("product_id").groupBy("product_id", "product_name").orderByDesc("sales").last("LIMIT 10"));

        List<Map<String, Object>> last30 = productOrderItemMapper.selectMaps(
                new QueryWrapper<ProductOrderItem>().select("product_id, SUM(quantity) AS s").isNotNull("product_id")
                        .ge("create_time", start).groupBy("product_id"));
        List<Map<String, Object>> prev30 = productOrderItemMapper.selectMaps(
                new QueryWrapper<ProductOrderItem>().select("product_id, SUM(quantity) AS s").isNotNull("product_id")
                        .ge("create_time", prevStart).lt("create_time", prevEnd).groupBy("product_id"));
        Map<Long, Long> lastSales = new HashMap<>(), prevSales = new HashMap<>();
        for (Map<String, Object> m : last30) lastSales.put(((Number) m.get("product_id")).longValue(), lng(m.get("s")));
        for (Map<String, Object> m : prev30) prevSales.put(((Number) m.get("product_id")).longValue(), lng(m.get("s")));

        List<Product> products = productMapper.selectList(null);
        Map<Long, Product> pmap = new HashMap<>();
        for (Product p : products) pmap.put(p.getId(), p);

        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> m : items) {
            Long pid = m.get("product_id") == null ? null : ((Number) m.get("product_id")).longValue();
            Product p = pid == null ? null : pmap.get(pid);
            long sales = lng(m.get("sales"));
            double amount = dbl(m.get("amount"));
            long last = lastSales.getOrDefault(pid, 0L);
            long prev = prevSales.getOrDefault(pid, 0L);
            double growth = prev > 0 ? Math.round((last - prev) * 1000.0 / prev) / 10.0 : (last > 0 ? 100.0 : 0.0);
            Map<String, Object> it = new HashMap<>();
            it.put("productName", m.get("product_name"));
            it.put("category", p == null ? null : p.getCategoryName());
            it.put("sales", sales);
            it.put("amount", amount);
            it.put("stock", p == null ? 0 : (p.getStock() == null ? 0 : p.getStock()));
            it.put("growth", growth);
            list.add(it);
        }
        return Result.success(PageResult.of((long) list.size(), list));
    }

    // ============ 营收分析（按月） ============
    @GetMapping("/revenue/page")
    public Result<PageResult<Map<String, Object>>> getRevenueAnalysis() {
        List<Map<String, Object>> pr = productOrderMapper.selectMaps(
                new QueryWrapper<ProductOrder>().select("DATE_FORMAT(create_time,'%Y-%m') AS m, SUM(payable_price) AS amt").groupBy("DATE_FORMAT(create_time,'%Y-%m')"));
        List<Map<String, Object>> sr = serviceOrderMapper.selectMaps(
                new QueryWrapper<ServiceOrder>().select("DATE_FORMAT(create_time,'%Y-%m') AS m, SUM(price) AS amt").groupBy("DATE_FORMAT(create_time,'%Y-%m')"));
        List<Map<String, Object>> cr = commissionRecordMapper.selectMaps(
                new QueryWrapper<CommissionRecord>().select("DATE_FORMAT(create_time,'%Y-%m') AS m, SUM(commission_amount) AS amt").groupBy("DATE_FORMAT(create_time,'%Y-%m')"));

        Map<String, Double> pRev = new HashMap<>(), sRev = new HashMap<>(), comm = new HashMap<>();
        for (Map<String, Object> m : pr) pRev.put((String) m.get("m"), dbl(m.get("amt")));
        for (Map<String, Object> m : sr) sRev.put((String) m.get("m"), dbl(m.get("amt")));
        for (Map<String, Object> m : cr) comm.put((String) m.get("m"), dbl(m.get("amt")));

        Set<String> months = new HashSet<>();
        months.addAll(pRev.keySet());
        months.addAll(sRev.keySet());
        months.addAll(comm.keySet());
        List<String> sorted = new ArrayList<>(months);
        Collections.sort(sorted, Collections.reverseOrder());

        List<Map<String, Object>> list = new ArrayList<>();
        for (String m : sorted) {
            double sv = sRev.getOrDefault(m, 0.0), pv = pRev.getOrDefault(m, 0.0), cv = comm.getOrDefault(m, 0.0);
            Map<String, Object> it = new HashMap<>();
            it.put("date", m);
            it.put("serviceRevenue", sv);
            it.put("productRevenue", pv);
            it.put("commission", cv);
            it.put("total", Math.round((sv + pv + cv) * 100) / 100.0);
            list.add(it);
        }
        return Result.success(PageResult.of((long) list.size(), list));
    }

    // ============ 员工业绩 ============
    @GetMapping("/performance/page")
    public Result<PageResult<Map<String, Object>>> getPerformance() {
        List<Map<String, Object>> rows = commissionRecordMapper.selectMaps(
                new QueryWrapper<CommissionRecord>().select("worker_id, worker_name, COUNT(*) AS orders, SUM(order_amount) AS amount, SUM(commission_amount) AS commission")
                        .groupBy("worker_id", "worker_name").orderByDesc("commission").last("LIMIT 10"));
        List<Map<String, Object>> evals = orderEvaluationMapper.selectMaps(
                new QueryWrapper<OrderEvaluation>().select("worker_id, AVG(score) AS sc").isNotNull("worker_id").groupBy("worker_id"));
        Map<Long, Double> scoreMap = new HashMap<>();
        for (Map<String, Object> m : evals) {
            if (m.get("worker_id") != null) scoreMap.put(((Number) m.get("worker_id")).longValue(), dbl(m.get("sc")));
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> m : rows) {
            Long wid = m.get("worker_id") == null ? null : ((Number) m.get("worker_id")).longValue();
            Map<String, Object> it = new HashMap<>();
            it.put("staffName", m.get("worker_name"));
            it.put("department", "");
            it.put("orders", lng(m.get("orders")));
            it.put("amount", dbl(m.get("amount")));
            it.put("commission", dbl(m.get("commission")));
            it.put("score", Math.round(scoreMap.getOrDefault(wid, 0.0) * 10) / 10.0);
            list.add(it);
        }
        return Result.success(PageResult.of((long) list.size(), list));
    }

    // ============ 用户年龄构成（真实生日） ============
    @GetMapping("/user-age-analysis")
    public Result<List<Map<String, Object>>> getUserAgeAnalysis() {
        List<Map<String, Object>> rows = appUserMapper.selectMaps(
                new QueryWrapper<AppUser>().select("birth_date").isNotNull("birth_date"));
        int[] buckets = new int[5];
        int total = 0;
        int curYear = LocalDate.now().getYear();
        for (Map<String, Object> m : rows) {
            Object bd = m.get("birth_date");
            if (!(bd instanceof Date)) continue;
            LocalDate b = ((Date) bd).toLocalDate();
            int age = curYear - b.getYear();
            int idx = age < 50 ? 0 : (age < 60 ? 1 : (age < 70 ? 2 : (age < 80 ? 3 : 4)));
            buckets[idx]++;
            total++;
        }
        String[] labels = {"50岁以下", "50-60岁", "60-70岁", "70-80岁", "80岁以上"};
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Map<String, Object> it = new HashMap<>();
            it.put("ageGroup", labels[i]);
            it.put("count", buckets[i]);
            it.put("percentage", total > 0 ? Math.round(buckets[i] * 1000.0 / total) / 10.0 : 0);
            list.add(it);
        }
        return Result.success(list);
    }

    // ============ 用户性别构成 ============
    @GetMapping("/user-gender-analysis")
    public Result<List<Map<String, Object>>> getUserGenderAnalysis() {
        List<Map<String, Object>> rows = appUserMapper.selectMaps(
                new QueryWrapper<AppUser>().select("gender, COUNT(*) AS c").groupBy("gender"));
        Map<Integer, Long> g = new HashMap<>();
        long total = 0;
        for (Map<String, Object> m : rows) {
            int k = m.get("gender") == null ? 0 : ((Number) m.get("gender")).intValue();
            long c = lng(m.get("c"));
            g.put(k, c);
            total += c;
        }
        int[] keys = {0, 1, 2};
        String[] names = {"未知", "男", "女"};
        List<Map<String, Object>> list = new ArrayList<>();
        for (int k : keys) {
            long c = g.getOrDefault(k, 0L);
            if (c == 0) continue;
            Map<String, Object> it = new HashMap<>();
            it.put("gender", names[k]);
            it.put("count", c);
            it.put("percentage", total > 0 ? Math.round(c * 1000.0 / total) / 10.0 : 0);
            list.add(it);
        }
        return Result.success(list);
    }

    // ============ 复购分析：近7天 ============
    @GetMapping("/repurchase-analysis")
    public Result<List<Map<String, Object>>> getRepurchaseAnalysis() {
        LocalDate today = LocalDate.now();
        Map<String, Long> newUsers = new HashMap<>();
        List<Map<String, Object>> nu = appUserMapper.selectMaps(
                new QueryWrapper<AppUser>().select("DATE(create_time) AS d, COUNT(*) AS c").groupBy("DATE(create_time)"));
        for (Map<String, Object> m : nu) newUsers.put(fmtDate(m.get("d")), lng(m.get("c")));

        List<Map<String, Object>> om = productOrderMapper.selectMaps(
                new QueryWrapper<ProductOrder>().select("user_id, DATE(create_time) AS d").isNotNull("user_id"));
        Map<String, Set<Long>> byDate = new HashMap<>();
        Map<Long, String> firstDate = new HashMap<>();
        for (Map<String, Object> m : om) {
            Long uid = m.get("user_id") == null ? null : ((Number) m.get("user_id")).longValue();
            String d = fmtDate(m.get("d"));
            if (uid == null) continue;
            byDate.computeIfAbsent(d, k -> new HashSet<>()).add(uid);
            String f = firstDate.get(uid);
            if (f == null || d.compareTo(f) < 0) firstDate.put(uid, d);
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            String d = today.minusDays(i).format(DATE_FMT);
            long nnu = newUsers.getOrDefault(d, 0L);
            Set<Long> active = byDate.getOrDefault(d, Collections.emptySet());
            long rep = active.stream().filter(u -> {
                String f = firstDate.get(u);
                return f != null && f.compareTo(d) < 0;
            }).count();
            Map<String, Object> it = new HashMap<>();
            it.put("date", d);
            it.put("newUsers", nnu);
            it.put("repurchaseUsers", rep);
            it.put("rate", nnu > 0 ? Math.round(rep * 1000.0 / nnu) / 10.0 : 0);
            list.add(it);
        }
        return Result.success(list);
    }

    // ============ 评价分析 ============
    @GetMapping("/evaluation-analysis")
    public Result<List<Map<String, Object>>> getEvaluationAnalysis() {
        List<Map<String, Object>> rows = orderEvaluationMapper.selectMaps(
                new QueryWrapper<OrderEvaluation>().select("score, COUNT(*) AS c").groupBy("score"));
        Map<Integer, Long> sc = new HashMap<>();
        long total = 0;
        for (Map<String, Object> m : rows) {
            int k = m.get("score") == null ? 0 : ((Number) m.get("score")).intValue();
            long c = lng(m.get("c"));
            sc.put(k, c);
            total += c;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (int s = 5; s >= 1; s--) {
            long c = sc.getOrDefault(s, 0L);
            Map<String, Object> it = new HashMap<>();
            it.put("score", s);
            it.put("count", c);
            it.put("percentage", total > 0 ? Math.round(c * 1000.0 / total) / 10.0 : 0);
            list.add(it);
        }
        return Result.success(list);
    }

    // ============ 用户社交分析：近7天动态 ============
    @GetMapping("/user-social-analysis")
    public Result<List<Map<String, Object>>> getUserSocialAnalysis() {
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> rows = dynamicMapper.selectMaps(
                new QueryWrapper<Dynamic>().select("DATE(create_time) AS d, COUNT(*) AS posts, SUM(like_count) AS likes").groupBy("DATE(create_time)"));
        Map<String, Map<String, Object>> map = new HashMap<>();
        for (Map<String, Object> m : rows) map.put(fmtDate(m.get("d")), m);
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            String d = today.minusDays(i).format(DATE_FMT);
            Map<String, Object> m = map.getOrDefault(d, Collections.emptyMap());
            Map<String, Object> it = new HashMap<>();
            it.put("date", d);
            it.put("posts", lng(m.get("posts")));
            it.put("comments", 0);
            it.put("likes", lng(m.get("likes")));
            list.add(it);
        }
        return Result.success(list);
    }

    // ===== 以下为系统/运营类占位（暂无对应种子数据表，保持结构兼容） =====
    @GetMapping("/export/page")
    public Result<PageResult<Map<String, Object>>> getExportList() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("id", 1);
        item.put("type", "用户数据");
        item.put("fileName", "user_data_20260701.xlsx");
        item.put("exportTime", "2026-07-01 10:30:00");
        item.put("status", "成功");
        list.add(item);
        return Result.success(PageResult.of((long) list.size(), list));
    }

    @GetMapping("/tracking/page")
    public Result<PageResult<Map<String, Object>>> getTrackingList() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("id", 1);
        item.put("eventName", "登录");
        item.put("eventCode", "login");
        item.put("count", 520);
        item.put("todayCount", 45);
        item.put("status", 1);
        list.add(item);
        Map<String, Object> item2 = new HashMap<>();
        item2.put("id", 2);
        item2.put("eventName", "下单");
        item2.put("eventCode", "order");
        item2.put("count", 167);
        item2.put("todayCount", 23);
        item2.put("status", 1);
        list.add(item2);
        return Result.success(PageResult.of((long) list.size(), list));
    }

    @GetMapping("/dict/page")
    public Result<PageResult<Map<String, Object>>> getDictList() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> m1 = new HashMap<>();
        m1.put("id", 1);
        m1.put("dictType", "性别");
        m1.put("dictCode", "gender");
        m1.put("dictLabel", "男");
        m1.put("dictValue", "1");
        m1.put("sort", 1);
        m1.put("status", 1);
        list.add(m1);
        Map<String, Object> m2 = new HashMap<>();
        m2.put("id", 2);
        m2.put("dictType", "性别");
        m2.put("dictCode", "gender");
        m2.put("dictLabel", "女");
        m2.put("dictValue", "2");
        m2.put("sort", 2);
        m2.put("status", 1);
        list.add(m2);
        return Result.success(PageResult.of((long) list.size(), list));
    }

    @GetMapping("/action-log/page")
    public Result<PageResult<Map<String, Object>>> getActionLogList() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("id", 1);
        item.put("userName", "admin");
        item.put("action", "登录系统");
        item.put("module", "系统管理");
        item.put("ip", "127.0.0.1");
        item.put("createTime", "2026-07-09 14:57:00");
        list.add(item);
        return Result.success(PageResult.of((long) list.size(), list));
    }

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDataDashboard() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalUsers", appUserMapper.selectCount(null));
        data.put("totalOrders", productOrderMapper.selectCount(null));
        data.put("totalRevenue", 0);
        data.put("totalWorkers", serviceWorkerMapper.selectCount(null));
        return Result.success(data);
    }
}
