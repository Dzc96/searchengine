package com.gdei.searchengine.controller;

import com.gdei.searchengine.core.Searcher;
import com.gdei.searchengine.domain.Result;
import com.gdei.searchengine.service.IndexService;
import com.gdei.searchengine.service.IndexServiceImpl;
import com.gdei.searchengine.service.SearchService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.xmlbeans.impl.xb.xsdschema.All;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

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
    SearchService searchService;

    @Autowired
    Searcher searcher;


//    public static Integer totalPage;




//    @GetMapping("/searchEngine")
//    public String search() {
//        return "search";
//    }

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
    //RequestMapping(value = "/searchFor", method = RequestMethod.POST)
    @RequestMapping(value = "/searchFor")
    public String createQuery(String parameter, Model model) throws Exception {
        //对传入参数做简单处理
        String target = parameter.replaceAll("\"","");
//        ArrayList<Result> results = searcher.search(IndexServiceImpl.indexDirectory, target);
        ArrayList<Result> Allresults = searchService.search(IndexServiceImpl.indexDirectory, target);
        ArrayList<Result> results = new ArrayList<>();
        //首页只返回五个
        for (int i = 0; i < 5; i++) {
            results.add(Allresults.get(i));
        }
        model.addAttribute("results", results);
        Integer totalPage = Allresults.size() / (Searcher.pageSize);
        model.addAttribute("totalPage", totalPage);
        return "search::table_refresh";
    }



    @GetMapping("/search")
    public String createQuery() throws Exception {
        return "search";
    }


    /**
     * 分页查询
     * @param page
     * @return
     */
    @GetMapping("/pageSearch")
//    public String pageQuery(@RequestParam("page")int page, @RequestParam("parameter")String parameter, Model model) throws Exception{
    public String pageQuery(Integer page, String parameter, Integer totalPage, Model model) throws Exception{

        //传入第几页，就能计算出，后台要返回第i条-->第j条数据
        //封装好这些数据返回，局部刷新页面即可

        System.out.println("进入请求");
        System.out.println("当前page:" + page);
        if (totalPage == null) {
            ArrayList<Result> Allresults = searchService.search(IndexServiceImpl.indexDirectory, parameter);
            Integer pageNumber = Allresults.size() / (Searcher.pageSize);
            totalPage = Allresults.size() % (Searcher.pageSize) == 0 ? pageNumber : (pageNumber+1);
            page = 1;
            System.out.println("本次查询一共有" + Allresults.size() + "记录");
        }

        String target = parameter.replaceAll("\"","");
        ArrayList<Result> results = searchService.pageSearch(IndexServiceImpl.indexDirectory, target, page);

        model.addAttribute("results", results);
        model.addAttribute("totalPage", totalPage);
        //要返回总页数，放到model里面
        return "search::table_refresh";
    }


//    @GetMapping("/pageInfoSearch")
////    public String pageQuery(@RequestParam("page")int page, @RequestParam("parameter")String parameter, Model model) throws Exception{
//    public String pageInfoSearch(Integer currentPage, String parameter, Model model) throws Exception{
//
//        //传入第几页，就能计算出，后台要返回第i条-->第j条数据
//        //封装好这些数据返回，局部刷新页面即可
//        System.out.println("进入请求");
//        String target = parameter.replaceAll("\"","");
//        ArrayList<Result> results = searchService.search(IndexServiceImpl.indexDirectory, target);
//
//        PageHelper.startPage(currentPage,2);
//        PageInfo<Result> pageInfo = new PageInfo<>(results);
//        model.addAttribute("results", results);
//
//        Integer startPage = 0;
//        Integer endPage = 0;
//        model.addAttribute("pageNum",pageInfo.getPageNum());
//        model.addAttribute("pageSize",pageInfo.getPageSize());
//        model.addAttribute("size",pageInfo.getSize());
//        model.addAttribute("startRow",pageInfo.getStartRow());
//        model.addAttribute("endRow",pageInfo.getEndRow());
//        model.addAttribute("total",pageInfo.getTotal());
//        model.addAttribute("pages",pageInfo.getPages());
//        model.addAttribute("prePage",pageInfo.getPrePage());
//        model.addAttribute("nextPage",pageInfo.getNextPage());
//        model.addAttribute("isFirstPage",pageInfo.isIsFirstPage());
//        model.addAttribute("isLastPage",pageInfo.isIsLastPage());
//        model.addAttribute("hasPreviousPage",pageInfo.isHasPreviousPage());
//        model.addAttribute("hasNextPage",pageInfo.isHasNextPage());
//        model.addAttribute("startPage",startPage);
//        model.addAttribute("endPage",endPage);
//        model.addAttribute("fenye", 1);
//        return "search::table_refresh";
//    }




}
