package com.campusconnect.system;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TrafficControlFilter extends OncePerRequestFilter {

    private final TrafficControlService trafficControlService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String uri = request.getRequestURI();

        // 管理接口、登录接口、健康检查接口不限制
        if (uri.startsWith("/api/admin")
                || uri.startsWith("/admin")
                || uri.startsWith("/api/auth")
                || uri.startsWith("/auth")
                || uri.startsWith("/api/actuator")
                || uri.startsWith("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (trafficControlService.needReject(uri)) {
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");

            response.getWriter().write(objectMapper.writeValueAsString(
                    Map.of(
                            "code", 429,
                            "message", "当前访问人数较多，请稍后再试"
                    )
            ));
            return;
        }

        filterChain.doFilter(request, response);
    }
}