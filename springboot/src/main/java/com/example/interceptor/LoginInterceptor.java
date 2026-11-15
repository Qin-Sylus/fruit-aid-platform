package com.example.interceptor;

import com.example.utils.JwtUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Resource
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 排除登录和注册接口
        String path = request.getRequestURI();
        if (path.equals("/login") || path.equals("/register")) {
            return true;
        }

        // 获取token
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(401);
            response.getWriter().write("未登录");
            return false;
        }

        // 验证token
        token = token.substring(7);
        if (!jwtUtil.validateToken(token)) {
            response.setStatus(401);
            response.getWriter().write("登录已过期");
            return false;
        }

        return true;
    }
}