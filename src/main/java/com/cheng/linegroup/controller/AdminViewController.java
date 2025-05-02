package com.cheng.linegroup.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 管理後台前端視圖控制器
 * 用於將所有前端路由重定向回 index.html，使 React Router 接管路由
 *
 * @author cheng
 * @since 2025/04/30
 */
@Controller
@RequestMapping("/admin")
public class AdminViewController {

    /**
     * 所有 /admin/** 路由都返回 index.html，由前端 React Router 處理實際路由
     * 
     * @return admin/index 模板
     */
    @GetMapping(value = {"", "/**"})
    public String forward() {
        return "admin/index";
    }
}
