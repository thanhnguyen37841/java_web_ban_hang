package com.hutech.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ApiDemoController {
    @GetMapping("/api-demo")
    public String apiDemo() {
        return "api-demo";
    }
}
