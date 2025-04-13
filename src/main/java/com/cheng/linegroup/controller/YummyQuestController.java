package com.cheng.linegroup.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 處理 YummyQuest 相關的請求
 * @author cheng
 * @since 2025/4/14
 **/
@Slf4j
@Controller
@RequestMapping("/YummyQuest")
public class YummyQuestController {

    /**
     * 處理 YummyQuest 首頁請求
     * @return YummyQuest/index 模板頁面
     */
    @GetMapping({"", "/"})
    public String index() {
        return "YummyQuest/index";
    }
}
