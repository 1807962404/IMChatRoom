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

    private String[] excludedUris;
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        excludedUris = filterConfig.getInitParameter("excludedUris").split(", ");
        log.info("SystemFilter init(), Excluded uris: {}", Arrays.toString(excludedUris));
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;

        // 定义flag变量，用于记录请求路径是否在 白名单（排除路径）中
        boolean flag = false;
        for (String excludedUri : excludedUris) {
            if (request.getRequestURI().equals(excludedUri)) {
                flag = true;    // 排除此请求路径，即过滤
                break;
            }
        }

        if (flag) {
            // 请求路径在白名单就放行
        } else {
//            log.info("SystemFilter：过滤的请求路径：{}", request.getRequestURI());
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
