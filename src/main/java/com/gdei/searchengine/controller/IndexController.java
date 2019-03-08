package com.gdei.searchengine.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class IndexController {


    @GetMapping("/search")
    public String search() {
        return "search";
    }



    @GetMapping("/search1")
    public String search1() {
        return "search1";
    }
}
