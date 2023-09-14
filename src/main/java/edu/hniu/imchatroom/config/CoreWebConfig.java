package edu.hniu.imchatroom.config;

import edu.hniu.imchatroom.interceptor.LoginInterceptor;
import edu.hniu.imchatroom.util.StringUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * Web核心配置类
 */
@Configuration
public class CoreWebConfig implements WebMvcConfigurer {

    /**
     * 将拦截器添加至容器中，并指定拦截规则
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 拦截路径，这里是所有的请求都会被拦截（包括静态资源）
        InterceptorRegistration interceptorRegistration =
                registry.addInterceptor(new LoginInterceptor()).addPathPatterns("/**");

        // 获取排除拦截的路径（注意需要的是一个字符串数组）
        interceptorRegistration.excludePathPatterns(StringUtil.getExclusivesArrayPath(LoginInterceptor.class));
    }

    /**
     * 注册一个ServerEndpointExporter组件用于WebSocket连接使用
     * @return
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    /**
     * 视图控制跳转器
     * @param registry
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/signin").setViewName("sign");
        registry.addViewController("/login").setViewName("sign");
        registry.addViewController("/").setViewName("main");
        registry.addViewController("/main").setViewName("main");
        registry.addViewController("/error").setViewName("error");
    }
}
