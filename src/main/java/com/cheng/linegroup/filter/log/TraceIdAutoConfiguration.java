package com.cheng.linegroup.filter.log;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Cheng
 * @since 2024/7/23 23:47
 **/
@Configuration
@ConditionalOnClass(value = DispatcherServlet.class)
public class TraceIdAutoConfiguration implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TraceIdHandlerInterceptor())
                .addPathPatterns("/**");
    }
}
