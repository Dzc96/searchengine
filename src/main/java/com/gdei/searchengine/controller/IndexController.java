package com.gdei.searchengine.controller;

import com.gdei.searchengine.service.IndexService;
import org.junit.runners.Parameterized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
public class IndexController {

    @Autowired
    IndexService indexService;

    @GetMapping("/search")
    public String search() {
        return "search";
    }



    @GetMapping("/search1")
    public String search1() {
        return "search1";
    }

    //post delete put  get  head trace options connect
    @RequestMapping(value = "/index", method = RequestMethod.POST)
    @ResponseBody
    public Object createIndex(@RequestBody String dataDirectory) throws Exception {
//        String target = dataDirectory;
//        String target = dataDirectory.trim().substring(0, dataDirectory.length()-1);
//        dataDirectory.replaceAll("\\\\\\\\", "\\\\");
//        dataDirectory.replaceAll("\"","");
//        target.substring(0, target.length()-2);
//
//        System.out.println("dataDirectory：" +   dataDirectory.replaceAll("\"",""));
//        System.out.println("dataDirectory变短后：" + dataDirectory.substring(0, dataDirectory.length()-3));
        String target = dataDirectory.replaceAll("\"","");
        String target1 = target.substring(0, target.length()-2);
        indexService.createIndex(target1);
        Map<String,Object> map = new HashMap<>();
        map.put("success",true);
        map.put("message","更新成功了！");
        return map;
    }

}
