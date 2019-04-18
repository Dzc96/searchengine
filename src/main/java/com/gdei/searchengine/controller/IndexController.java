package com.gdei.searchengine.controller;

import com.gdei.searchengine.core.Searcher;
import com.gdei.searchengine.domain.Result;
import com.gdei.searchengine.service.IndexService;
import com.gdei.searchengine.service.IndexServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Controller
public class IndexController {

    @Autowired
    IndexService indexService;

    @Autowired
    Searcher searcher;

    @GetMapping("/searchEngine")
    public String search1() {
        return "search";
    }



    /**
     * 处理前台的ajax请求，对传到后台的路径目录进行特定处理
     * @param dataDirectory
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/index", method = RequestMethod.POST)
    @ResponseBody
    public Object createIndex(@RequestBody String dataDirectory) throws Exception {
        String target = dataDirectory.replaceAll("\"","");
        String target1 = target.substring(0, target.length()-2);
        indexService.createIndex(target1);
        Map<String,Object> map = new HashMap<>();
        map.put("success",true);
        map.put("message","索引创建成功！");
        return map;
    }


    /**
     * 处理前台的ajax请求，对传到后台的关键字进行查询，返回查询结果，考虑ajax的回调函数去打开新的页面
     * @param parameter
     * @return
     * @throws Exception
     */
//    @RequestMapping(value = "/searchFor", method = RequestMethod.POST)
//    @ResponseBody
//    public Object createQuery(@RequestBody String parameter) throws Exception {
//        //对传入参数做简单处理
//        String target = parameter.replaceAll("\"","");
//        ArrayList<Result> results = searcher.search(IndexServiceImpl.indexDirectory, target);
//        Map<String,Object> map = new HashMap<>();
//
//        map.put("success",true);
//        map.put("message","查询成功！");
//        return map;
//    }


    @RequestMapping(value = "/searchFor", method = RequestMethod.POST)
    public String createQuery(@RequestBody String parameter, HttpServletRequest request, HttpServletResponse response) throws Exception {
        //对传入参数做简单处理
        String target = parameter.replaceAll("\"","");
        ArrayList<Result> results = searcher.search(IndexServiceImpl.indexDirectory, target);
        request.setAttribute("results", results);
        return "queryResult";
    }

//    @RequestMapping("/queryResult")
//    public String queryResult(Model model, @RequestParam("results") ArrayList results) {
//        model.addAttribute("results", results);
//        return "queryResult";
//    }


    @RequestMapping(value = "/searchFor111")
    public String createQuery1() throws Exception {

        return "queryResult";
    }





}
