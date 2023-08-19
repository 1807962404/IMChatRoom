package edu.hniu.imchatroom.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import static edu.hniu.imchatroom.util.VariableUtil.*;

@Slf4j
public class LoginInterceptor implements HandlerInterceptor {
    /**
     * 在目标方法之前执行
     * @param request current HTTP request
     * @param response current HTTP response
     * @param handler chosen handler to execute, for type and/or instance evaluation
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        HttpSession session = request.getSession();

        if (null == session || null == session.getAttribute(SIGNINED_USER)) {
            log.info("拦截的请求为：{}", request.getRequestURI());
            // 未登录则跳转至登陆页
//            ResultVO resultVO = new ResultVO<>(-1, "请先登陆！");
            request.setAttribute("msg", "请先登陆！");
            request.getRequestDispatcher("/login").forward(request, response);
            return false; // 拦截
        }

        return true;
    }
}
