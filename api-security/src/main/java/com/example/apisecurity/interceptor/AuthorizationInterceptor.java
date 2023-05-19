package com.example.apisecurity.interceptor;

import com.example.apisecurity.exception.NoBearerError;
import com.example.apisecurity.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
@Component
public class AuthorizationInterceptor implements HandlerInterceptor {
    private final AuthService authService;

    public AuthorizationInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response
            , Object handler) throws Exception {
        String handlerString= request.getHeader("Authorization");
        if(handlerString == null ||
                !handlerString.startsWith("Bearer ")){
            throw new NoBearerError();
        }
        request.setAttribute("user",
                authService.getUserFromToken(handlerString.substring(7)));
        return true;
    }
}