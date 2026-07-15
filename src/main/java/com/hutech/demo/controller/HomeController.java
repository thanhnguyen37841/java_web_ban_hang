package com.hutech.demo.controller;

import com.hutech.demo.service.CategoryService;
import com.hutech.demo.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("featuredProducts", productService.getFeaturedProducts());
        model.addAttribute("newProducts", productService.getNewProducts());
        model.addAttribute("saleProducts", productService.getSaleProducts());
        return "home/home";
    }
}
