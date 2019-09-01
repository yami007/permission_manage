package com.permission.common;

import com.permission.exception.ParamException;
import com.permission.exception.PermissionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class SpringExceptionResolver implements HandlerExceptionResolver {
    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String url = request.getRequestURL().toString();
        ModelAndView modelAndView = null;
        String defauleMsg = "System error";
        // 要求项目中所有的请求json都使用.json结尾
        if (url.endsWith(".json")) {
            if (ex instanceof PermissionException || ex instanceof ParamException) {
                log.error("unknow jsonexception ,url:" + url, ex);
                JsonData jsonData = JsonData.fail(ex.getMessage());
                modelAndView = new ModelAndView("jsonView", jsonData.toMap());
            } else { //未知异常
                log.error("unknow exception ,url:" + url, ex);
                JsonData jsonData = JsonData.fail(defauleMsg);
                modelAndView = new ModelAndView("jsonView", jsonData.toMap());
            }
        } else if (url.endsWith(".page")) {
            log.error("unknow pagexception ,url:" + url, ex);
            JsonData jsonData = JsonData.fail(ex.getMessage());
            modelAndView = new ModelAndView("exception", jsonData.toMap());
        } else {
            log.error("unknow exception ,url:" + url, ex);
            JsonData jsonData = JsonData.fail(ex.getMessage());
            modelAndView = new ModelAndView("jsonView", jsonData.toMap());
        }
        return modelAndView;
    }
}
