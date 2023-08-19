package edu.hniu.imchatroom.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Arrays;

import static edu.hniu.imchatroom.util.VariableUtil.ADMIN_USER_NAME;
import static edu.hniu.imchatroom.util.VariableUtil.COMMON_USER_NAME;

@Slf4j
public class SystemFilter implements Filter {

    private FilterConfig filterConfig;
    private HttpServletRequest request;
    private HttpServletResponse response;

    private String[] excludedUris;
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
        excludedUris = filterConfig.getInitParameter("excludedUris").split(", ");
        log.info("SystemFilter init(), Excluded uris: {}", Arrays.toString(excludedUris));
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        request = (HttpServletRequest) servletRequest;
        response = (HttpServletResponse) servletResponse;

        // 定义flag变量，用于记录请求路径是否在 白名单（排除路径）中
        boolean flag = false;
        for (String excludedUri : excludedUris) {
            if (request.getRequestURI().equals(excludedUri)) {
                flag = true;    // 排除此请求路径，即过滤
                break;
            }
        }

        /*// 设置管理员和普通用户名称，用于前端校验
        HttpSession session = request.getSession();
        if (null == session.getAttribute(ADMIN_USER_NAME)) {
            session.setAttribute(ADMIN_USER_NAME, ADMIN_USER_NAME);
        }
        if (null == session.getAttribute(COMMON_USER_NAME)) {
            session.setAttribute(COMMON_USER_NAME, COMMON_USER_NAME);
        }*/

        if (flag) {
            // 请求路径在白名单就放行
        } else {
//            log.info("SystemFilter：过滤的请求路径：{}", request.getRequestURI());
        }
        filterChain.doFilter(servletRequest,servletResponse);
    }

    @Override
    public void destroy() {
        if (null != request) {
            log.info("已销毁本次会话：{}", request.getSession().getId());
            request.getSession().invalidate();
        }
        Filter.super.destroy();
    }
}
