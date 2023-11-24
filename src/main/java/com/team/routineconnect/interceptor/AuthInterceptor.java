package com.team.routineconnect.interceptor;

import com.team.routineconnect.config.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        final String token = request.getHeader("TOKEN");
        if (!jwtTokenProvider.validateToken(token)) {
            throw new Exception("권한이 없습니다.");
        }
        request.setAttribute("userID", jwtTokenProvider.getUsername(token));
        return true;
    }
}
