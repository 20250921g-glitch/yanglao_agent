package com.care.common.aspect;

import com.care.common.cache.CacheHelper;
import com.care.common.result.Result;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据看板 / 报表聚合缓存切面。
 *
 * 仅缓存 DataAnalysisController 下的只读 GET 聚合接口（code=200 的成功结果），
 * 按方法名维度缓存，TTL 依接口敏感度区分。安全过滤器（Spring Security）在切面之前执行，
 * 故缓存命中不会绕过鉴权；聚合结果为全局一致数据，对所有已认证管理员相同。
 *
 * 设计取舍：采用固定 TTL 而非写后失效。报表/看板本就是「快照」，分钟级陈旧是可接受的，
 * 这样可避免把缓存失效耦合进下单、支付、评价等所有写路径，降低改动风险。
 */
@Aspect
@Component
@Order(1)
public class DataCacheAspect {

    @Autowired
    private CacheHelper cacheHelper;

    private static final String PREFIX = "data:";

    /** 各报表接口缓存时长（分钟）。看板实时性要求高用短 TTL，分布类用长 TTL。 */
    private static final Map<String, Long> TTL = new HashMap<>();
    static {
        TTL.put("getDataDashboard", 5L);
        TTL.put("getUserAnalysis", 10L);
        TTL.put("getTradeAnalysis", 10L);
        TTL.put("getServiceAnalysis", 10L);
        TTL.put("getProductAnalysis", 10L);
        TTL.put("getRevenueAnalysis", 10L);
        TTL.put("getPerformance", 10L);
        TTL.put("getWorkerRank", 10L);
        TTL.put("getPayMethod", 30L);
        TTL.put("getHourly", 30L);
        TTL.put("getAreaRank", 30L);
        TTL.put("getUserAgeAnalysis", 30L);
        TTL.put("getUserGenderAnalysis", 30L);
        TTL.put("getRepurchaseAnalysis", 30L);
        TTL.put("getEvaluationAnalysis", 30L);
        TTL.put("getUserSocialAnalysis", 30L);
        TTL.put("getExportList", 60L);
        TTL.put("getTrackingList", 60L);
        TTL.put("getDictList", 60L);
        TTL.put("getActionLogList", 60L);
    }
    private static final long DEFAULT_TTL = 10L;

    @Around("execution(* com.care.module.data.controller.DataAnalysisController.*(..))")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        String methodName = ((MethodSignature) pjp.getSignature()).getName();
        String key = PREFIX + methodName;
        Object cached = cacheHelper.get(key);
        if (cached != null) {
            return cached;
        }
        Object result = pjp.proceed();
        if (result instanceof Result
                && ((Result<?>) result).getCode() != null
                && ((Result<?>) result).getCode() == 200) {
            long ttl = TTL.getOrDefault(methodName, DEFAULT_TTL);
            cacheHelper.put(key, result, ttl);
        }
        return result;
    }
}
