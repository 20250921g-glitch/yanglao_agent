package com.care.common.aspect;

import com.care.common.util.JwtUtil;
import com.care.module.sys.entity.SysOperationLog;
import com.care.module.sys.service.SysOperationLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 操作日志切面：对所有 Controller 的写操作（POST/PUT/DELETE）真实落库。
 * 模块取自控制器 @RequestMapping，操作描述由请求路径推断。
 */
@Aspect
@Component
public class OperationLogAspect {

    private static final Set<String> WRITE_METHODS = new java.util.HashSet<java.lang.String>(
            java.util.Arrays.asList("POST", "PUT", "DELETE"));

    @Autowired
    private SysOperationLogService operationLogService;
    @Autowired
    private JwtUtil jwtUtil;

    @Around("execution(* com.care.module..controller..*(..))")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return pjp.proceed();
        }
        HttpServletRequest request = attrs.getRequest();
        String method = request.getMethod();
        String uri = request.getRequestURI();
        // 仅记录写操作，且跳过日志接口自身
        if (!WRITE_METHODS.contains(method) || uri.contains("/operation-log")) {
            return pjp.proceed();
        }

        String module = resolveModule(pjp);
        String operation = resolveOperation(request, uri);

        boolean success = true;
        String errorMsg = null;
        Object result;
        try {
            result = pjp.proceed();
        } catch (Throwable t) {
            success = false;
            errorMsg = t.getMessage();
            if (errorMsg != null && errorMsg.length() > 480) errorMsg = errorMsg.substring(0, 480);
            throw t;
        } finally {
            try {
                record(request, module, operation, success, errorMsg);
            } catch (Exception ignore) {
                // 日志写入失败不影响主流程
            }
        }
        return result;
    }

    private void record(HttpServletRequest request, String module, String operation, boolean success, String errorMsg) {
        SysOperationLog log = new SysOperationLog();
        log.setModule(module);
        log.setOperation(operation);
        log.setMethod(request.getMethod());
        log.setRequestUri(request.getRequestURI());
        log.setIp(getClientIp(request));
        log.setStatus(success ? 1 : 0);
        log.setErrorMsg(errorMsg);
        log.setCreateTime(LocalDateTime.now());

        // 操作用户：优先取过滤器注入的 attribute，缺失时回退解析 token
        Object uid = request.getAttribute("userId");
        Object uname = request.getAttribute("username");
        if (uid == null || uname == null) {
            String token = request.getHeader("token");
            if (token == null) token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) token = token.substring(7);
            if (token != null && !token.isEmpty()) {
                try {
                    if (uid == null) uid = jwtUtil.getUserIdFromToken(token);
                    if (uname == null) uname = jwtUtil.getUsernameFromToken(token);
                } catch (Exception ignore) {
                }
            }
        }
        log.setUserId(uid == null ? null : ((Number) uid).longValue());
        log.setUserName(uname == null ? "system" : String.valueOf(uname));

        operationLogService.save(log);
    }

    private String resolveModule(ProceedingJoinPoint pjp) {
        Class<?> clazz = pjp.getSignature().getDeclaringType();
        RequestMapping rm = clazz.getAnnotation(RequestMapping.class);
        if (rm != null && rm.value().length > 0) {
            String base = rm.value()[0];
            // /api/trade/product-order -> trade/product-order
            if (base.startsWith("/api/")) base = base.substring(5);
            return base;
        }
        return clazz.getSimpleName().replace("Controller", "");
    }

    private String resolveOperation(HttpServletRequest request, String uri) {
        String method = request.getMethod();
        String lower = uri.toLowerCase();
        if (lower.endsWith("/login") || lower.contains("/login")) return "登录系统";
        if (lower.contains("/logout")) return "退出登录";
        if (lower.contains("/status")) return "状态变更";
        if (lower.contains("/close")) return "关闭";
        if (lower.contains("/reset")) return "重置";
        if (lower.contains("/export")) return "导出";
        if (lower.contains("/import")) return "导入";
        if (lower.contains("/audit")) return "审核";
        // 无关键字时按 HTTP 方法给出语义化默认
        if ("POST".equals(method)) return "新增";
        if ("PUT".equals(method) || "PATCH".equals(method)) return "修改";
        if ("DELETE".equals(method)) return "删除";
        return "操作(" + (uri.contains("?") ? uri.substring(0, uri.indexOf("?")) : uri) + ")";
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) ip = ip.split(",")[0].trim();
        return ip;
    }
}
