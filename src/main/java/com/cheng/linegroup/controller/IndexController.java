package com.cheng.linegroup.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author cheng
 * @since 2024/2/17 23:27
 **/
@Slf4j
@Controller
@RequestMapping("/")
public class IndexController {

    @GetMapping()
    public ModelAndView index() {
        return new ModelAndView("index");
    }
}
