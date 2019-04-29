package com.gdei.searchengine.controller;

import com.gdei.searchengine.core.Searcher;
import com.gdei.searchengine.domain.Result;
import com.gdei.searchengine.service.IndexService;
import com.gdei.searchengine.service.IndexServiceImpl;
import com.gdei.searchengine.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Controller
public class SearchEngineController {

    @Autowired
    IndexService indexService;

    @Autowired
    SearchService searchService;

    @Autowired
    Searcher searcher;

    @GetMapping("/search")
    public String createQuery() throws Exception {
        return "search";
    }


    /**
     * 处理前台的ajax请求，对传到后台的路径目录进行特定处理，然后创建索引
     *
     * @param dataDirectory
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/index")
    @ResponseBody
    public Object createIndex(@RequestBody String dataDirectory) throws Exception {
        String target = dataDirectory.replaceAll("\"", "");
        String target1 = target.substring(0, target.length() - 2);
        indexService.createIndex(target1);
        Map<String, Object> map = new HashMap<>();
        map.put("success", true);
        map.put("message", "索引创建成功！");
        return map;
    }


    /**
     * 根据传到后台的关键字进行查询。
     *
     * @param parameter
     * @return
     * @throws Exception
     */
    //RequestMapping(value = "/searchFor", method = RequestMethod.POST)
    @PostMapping("/searchFor")
    public String createQuery(String parameter, Model model) throws Exception {
        //对传入参数做简单处理
        String target = parameter.replaceAll("\"", "");
        List<Result> Allresults = searchService.search(IndexServiceImpl.indexDirectory, target);

        //讲道理这里是可以优化的。
        //首页只返回五个
//        ArrayList<Result> results = searchService.pageSesarch(IndexServiceImpl.indexDirectory, target,1);
        List<Result> results = searchService.booleanSearch(IndexServiceImpl.indexDirectory, target, 1);
        model.addAttribute("results", results);
        Integer totalPage = Allresults.size() / (Searcher.PAGE_SIZE);
        model.addAttribute("totalPage", totalPage);

        return "search::table_refresh";
    }


    /**
     * 分页查询
     *
     * @param page
     * @return
     */
    @GetMapping("/pageSearch")
    public String pageQuery(Integer page, String parameter, Integer totalPage, Integer totalNumber, Model model) throws Exception {

        //传入第几页，就能计算出，后台要返回第i条-->第j条数据
        //封装好这些数据返回，局部刷新页面即可

        if (totalPage == null) { //说明第一次查询
            List<Result> Allresults = searchService.search(IndexServiceImpl.indexDirectory, parameter);
            Integer pageNumber = Allresults.size() / (Searcher.PAGE_SIZE);
            totalPage = Allresults.size() % (Searcher.PAGE_SIZE) == 0 ? pageNumber : (pageNumber + 1);
            page = 1;
            System.out.println("本次查询一共有" + Allresults.size() + "记录");
            totalNumber = Allresults.size();
        }

        String target = parameter.replaceAll("\"", "");
//        ArrayList<Result> results = searchService.pageSearch(IndexServiceImpl.indexDirectory, target, page);
        List<Result> results = searchService.booleanSearch(IndexServiceImpl.indexDirectory, target, page);

        model.addAttribute("results", results);
        model.addAttribute("totalPage", totalPage);
        model.addAttribute("totalNumber", totalNumber);
        //要返回总页数，放到model里面
        return "search::table_refresh";
    }


    @PostMapping("/suggestSearch")
    @ResponseBody
    public List<Result> keySuggest(@RequestBody String key) throws Exception {
        List<Result> suggests = searchService.suggestSearch(key);
        System.out.println("results的大小：" + suggests.size());
        Iterator<Result> iterator = suggests.iterator();

        while (iterator.hasNext()) {
            Result next = iterator.next();
            System.out.println(next.getFileName());
        }
        return suggests;
    }


}
