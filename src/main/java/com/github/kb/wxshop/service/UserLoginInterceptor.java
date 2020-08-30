package com.github.kb.wxshop.service;

import com.github.kb.wxshop.generate.User;
import org.apache.shiro.SecurityUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserLoginInterceptor implements HandlerInterceptor {
    private UserService userService;

    public UserLoginInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Object tel = SecurityUtils.getSubject().getPrincipal();
        // 如果tel不是null 说明已经登陆了
        if (tel != null) {
            User user = userService.getUserByTel(tel.toString());
            UserContext.setCurrentUser(user);
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 线程是会被复用的 如果线程1中保存了用户A的信息 并且没有被清理的话 下次线程1再用来处理别的请求的时候 就会出现"串号"的情况
        UserContext.setCurrentUser(null);
    }
}
