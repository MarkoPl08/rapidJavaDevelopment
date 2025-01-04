package com.grade.rapidjavadevelopment.mvc.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/")
    public String showDashboard() {
        return "redirect:/courses";
    }
}