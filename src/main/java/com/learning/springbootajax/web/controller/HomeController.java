package com.learning.springbootajax.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String init() {
//        return "promo-add";
        return "redirect:/promocao/add";
    }

}
