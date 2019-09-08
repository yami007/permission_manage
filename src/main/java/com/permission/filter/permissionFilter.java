package com.permission.filter;

import com.google.common.base.Splitter;
import com.permission.common.ApplicationContextHelper;
import com.permission.common.JsonData;
import com.permission.common.RequestHolder;
import com.permission.model.SysUser;
import com.permission.service.SysCoreService;
import com.permission.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 权限拦截过滤器
 */
@Slf4j
public class permissionFilter implements Filter {
    private static Set<String> exclusionUrlSet = new HashSet<>();

    private final static String noAuthUrl = "/sys/user/noAuth.page";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String exclusionUrls = filterConfig.getInitParameter("exclusionUrls");
        List<String> exclusionUrlList = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(exclusionUrls);
        exclusionUrlSet.addAll(exclusionUrlList);
        exclusionUrlSet.add(noAuthUrl);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        // 获取url
        String servletPath = request.getServletPath();
        // 获取参数
        Map parameterMap = request.getParameterMap();
        // 判断路径是否在排除校验的路径里
        if (exclusionUrlSet.contains(servletPath)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        // 获取当前用户
        SysUser sysUser = RequestHolder.getCurrentUser();
        // 如果当前用于为空，则跳转到无权限访问页面
        if (sysUser == null) {
            log.info("someOne visit {},but no login ,parameter:{}", servletPath, JsonUtils.obj2String(parameterMap));
            noAuth(request, response);
            return;
        }
        // 判断当前用户是否有这个路径的权限
        SysCoreService sysCoreService = ApplicationContextHelper.getBean(SysCoreService.class);
        Boolean hasUrlAcl = sysCoreService.hasUrlAcl(servletPath);
        // 如果没有，则跳转到无权限访问页面
        if (!hasUrlAcl) {
            log.info("someOne visit {},but no permission ,parameter:{}", servletPath, JsonUtils.obj2String(parameterMap));
            noAuth(request, response);
            return;
        }
        // 有权限，放行
        filterChain.doFilter(servletRequest, servletResponse);
    }

    /**
     * 跳转到无权限访问页面
     * @param request
     * @param response
     * @throws IOException
     */
    private void noAuth(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String servletPath = request.getServletPath();
        // 如果是json请求
        if (servletPath.endsWith(".json")) {
            JsonData jsonData = JsonData.fail("没有权限访问，如需要访问，请联系管理员");
            response.setHeader("Content-Type", "application/json");
            response.getWriter().print(JsonUtils.obj2String(jsonData));
            return;
        } else { // 如果是页面请求
            clientRedirect(noAuthUrl, response);
            return;
        }
    }

    private void clientRedirect(String url, HttpServletResponse response) throws IOException {
        response.setHeader("Content-Type", "text/html");
        response.getWriter().print("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n"
                + "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" + "<head>\n" + "<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"/>\n"
                + "<title>跳转中...</title>\n" + "</head>\n" + "<body>\n" + "跳转中，请稍候...\n" + "<script type=\"text/javascript\">//<![CDATA[\n"
                + "window.location.href='" + url + "?ret='+encodeURIComponent(window.location.href);\n" + "//]]></script>\n" + "</body>\n" + "</html>\n");
    }

    @Override
    public void destroy() {

    }
}
