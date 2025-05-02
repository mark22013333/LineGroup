package com.cheng.linegroup.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 管理後台前端配置
 * 處理靜態資源和前端路由
 *
 * @author cheng
 * @since 2025/04/30
 */
@Configuration
public class AdminConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置 React 應用靜態資源路徑
        registry.addResourceHandler("/react-admin/**")
                .addResourceLocations("classpath:/static/react-admin/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 將 /admin 路由映射到 admin/index 視圖
        registry.addViewController("/admin").setViewName("admin/index");
        // 確保所有 /admin/** 路由都使用相同的視圖，以支持前端路由
        registry.addViewController("/admin/**").setViewName("admin/index");
    }
}
