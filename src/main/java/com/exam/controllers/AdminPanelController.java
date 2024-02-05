package com.exam.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminPanelController {
    @GetMapping("/admin")
    public String adminHome() {
        // This will return a view called 'admin-panel'
        return "admin-panel";
    }
}