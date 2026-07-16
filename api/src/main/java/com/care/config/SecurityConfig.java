package com.care.config;

import com.care.common.filter.JwtAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("Configuring SecurityFilterChain...");
        
        http
            .csrf().disable()
            .authorizeRequests()
                .antMatchers("/api/sys/user/login").permitAll()
                .antMatchers("/api/app/user/register", "/api/app/user/login").permitAll()
                .antMatchers("/api/app/captcha", "/api/app/sms/send").permitAll()
                .antMatchers("/api/test/**").permitAll()
                .antMatchers("/doc.html", "/swagger-ui/**", "/swagger-resources/**",
                             "/v2/api-docs", "/webjars/**", "/favicon.ico",
                             "/webjars/springfox-openapi/**", "/v2/api-docs-ext/**").permitAll()
                .anyRequest().authenticated()
                .and()
            .formLogin().disable()
            .httpBasic().disable()
            .exceptionHandling()
                .authenticationEntryPoint((request, response, authException) -> {
                    log.warn("Authentication entry point: {}", authException.getMessage());
                    response.setStatus(401);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":401,\"message\":\"Unauthorized\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    log.warn("Access denied: {}", accessDeniedException.getMessage());
                    response.setStatus(403);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":403,\"message\":\"Access Denied: " + accessDeniedException.getMessage() + "\"}");
                })
                .and()
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        log.info("SecurityFilterChain configured");
        return http.build();
    }
}
