package com.care.common.filter;

import com.care.common.util.JwtUtil;
import com.care.module.sys.service.PermissionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PermissionService permissionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 获取请求路径
        String requestURI = request.getRequestURI();

        // 白名单路径，直接放行
        if (isWhiteList(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 获取token
        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
        }

        // 验证token
        if (token == null || token.isEmpty()) {
            writeUnauthorized(response, "未登录或登录已过期");
            return;
        }

        try {
            if (!jwtUtil.validateToken(token)) {
                writeUnauthorized(response, "Token无效或已过期");
                return;
            }

            // 将用户信息放入request
            Long userId = jwtUtil.getUserIdFromToken(token);
            String username = jwtUtil.getUsernameFromToken(token);
            request.setAttribute("userId", userId);
            request.setAttribute("username", username);

            // 动态获取用户角色
            List<String> roles = permissionService.getUserRoles(userId);
            List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

            // 关键：将Authentication设置到SecurityContext
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(
                    username, 
                    null, 
                    authorities
                );
            authentication.setDetails(userId);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            writeUnauthorized(response, "Token验证失败: " + e.getMessage());
        }
    }

    private boolean isWhiteList(String uri) {
        return uri.startsWith("/api/sys/user/login") ||
               uri.startsWith("/api/app/user/register") ||
               uri.startsWith("/api/app/user/login") ||
               uri.startsWith("/api/app/captcha") ||
               uri.startsWith("/api/app/sms/send") ||
               uri.startsWith("/api/test/") ||
               uri.startsWith("/doc.html") ||
               uri.startsWith("/swagger-ui") ||
               uri.startsWith("/swagger-resources") ||
               uri.startsWith("/v2/api-docs") ||
               uri.startsWith("/webjars") ||
               uri.startsWith("/favicon.ico");
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> result = new HashMap<>();
        result.put("code", 401);
        result.put("message", message);
        result.put("data", null);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
