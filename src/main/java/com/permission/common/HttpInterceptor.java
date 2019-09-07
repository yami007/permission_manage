package com.permission.common;

import com.permission.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Slf4j
public class HttpInterceptor extends HandlerInterceptorAdapter {
    private static final String START_TIME = "requestStartTime";

    //在业务处理器处理请求之前被调用。预处理，可以进行编码、安全控制等处理；
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String url = request.getRequestURI().toString();
        Map parameterMap = request.getParameterMap();
        log.info("request start. url:{}, params:{}", url, JsonUtils.obj2String(parameterMap));
        long start = System.currentTimeMillis();
        request.setAttribute(START_TIME, start);
        return true;
    }

    //在业务处理器处理请求执行完成后，生成视图之前执行。后处理（调用了Service并返回ModelAndView，但未进行页面渲染），有机会修改ModelAndView；
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        String url = request.getRequestURI().toString();
        long start = (Long) request.getAttribute(START_TIME);
        long end = System.currentTimeMillis();
        log.info("request finished. url:{}, cost:{}", url, end - start);
        removeThreadLocalInfo();
    }

    //在DispatcherServlet完全处理完请求后被调用，可用于清理资源等。返回处理（已经渲染了页面），可以根据ex是否为null判断是否发生了异常，进行日志记录；
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        String url = request.getRequestURI().toString();
        long start = (Long) request.getAttribute(START_TIME);
        long end = System.currentTimeMillis();
        log.info("request completed. url:{}, cost:{}", url, end - start);
        removeThreadLocalInfo();
    }

    public void removeThreadLocalInfo() {
        RequestHolder.remove();
    }

}
