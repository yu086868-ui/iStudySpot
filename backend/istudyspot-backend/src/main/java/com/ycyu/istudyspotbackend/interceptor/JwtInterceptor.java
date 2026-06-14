package com.ycyu.istudyspotbackend.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ycyu.istudyspotbackend.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (jwtUtils.validateToken(token)) {
                Long userId = jwtUtils.getUserIdFromToken(token);
                request.setAttribute("userId", userId);
                return true;
            }
        }

        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(buildUnauthorizedPayload(request));
        return false;
    }

    private String buildUnauthorizedPayload(HttpServletRequest request) throws Exception {
        String requestUri = request.getRequestURI();
        if (requestUri != null && requestUri.startsWith("/api/agent/")) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("code", "UNAUTHORIZED");
            error.put("retryable", false);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("schemaVersion", "1.0");
            data.put("error", error);

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("code", 401);
            payload.put("message", "UNAUTHORIZED");
            payload.put("data", data);
            return OBJECT_MAPPER.writeValueAsString(payload);
        }

        return "{\"code\":401,\"message\":\"未登录或token失效\",\"data\":null}";
    }
}
