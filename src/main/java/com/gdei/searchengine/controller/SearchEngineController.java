package com.gdei.searchengine.controller;

import com.gdei.searchengine.core.PinYin;
import com.gdei.searchengine.core.Searcher;
import com.gdei.searchengine.core.Trie;
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

    static Trie trie;

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
        long start = System.currentTimeMillis();
        indexService.createIndex(target1);
        long end = System.currentTimeMillis();
        System.out.println("创建索引花费了" + (end-start)/1000 + "秒");
        Map<String, Object> map = new HashMap<>();
        map.put("success", true);
        map.put("message", "索引创建成功，耗时"+ (end-start)/1000 + "秒");
        return map;
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

        String target = parameter.replaceAll("\"", "").trim();

        if (totalPage == null) { //说明第一次查询
            List<Result> Allresults = searchService.search(IndexServiceImpl.indexDirectory, parameter);
            Integer pageNumber = Allresults.size() / (Searcher.PAGE_SIZE);
            totalPage = Allresults.size() % (Searcher.PAGE_SIZE) == 0 ? pageNumber : (pageNumber + 1);
            page = 1;
            System.out.println("本次查询一共有" + Allresults.size() + "记录");
            totalNumber = Allresults.size();

            //把查询关键字，转为拼音，放入两棵字典树
            String py = PinYin.getPinYin(target);
            String pyHeader = PinYin.getPinYinHeadChar(target);
            Searcher.trie.insert(py);
            Searcher.abbrTrie.insert(pyHeader);
            System.out.println("py是：" + py);
            System.out.println("搜索关键字是：" + target);
            Searcher.cn2pinyin.put(py, target);//保存拼音和中文的映射，方便返回数据时从拼音转为中文进行显示
            Searcher.cn2pinyin.put(pyHeader, target);
            for (String key : Searcher.cn2pinyin.keySet()) {
                System.out.println(key + ":" + Searcher.cn2pinyin.get(key));
            }

        }


//        ArrayList<Result> results = searchService.pageSearch(IndexServiceImpl.indexDirectory, target, page);
        List<Result> results = searchService.booleanSearch(IndexServiceImpl.indexDirectory, target, page);

        model.addAttribute("results", results);
        model.addAttribute("totalPage", totalPage);
        model.addAttribute("totalNumber", totalNumber);
        //要返回总页数，放到model里面
        return "search::table_refresh";
    }




    /**
     * 处理页面发送过来的关键字自动补全请求
     * @param key
     * @return
     * @throws Exception
     */
    @PostMapping("/suggestSearch")
    @ResponseBody
    public List<Result> keySuggest(@RequestBody String key) throws Exception {
        String target = key.replaceAll("\"", "");
        List<Result> suggests = searchService.suggestSearchByTrie(target);

        if (suggests == null)
            return null;

        System.out.println("results的大小：" + suggests.size());
        Iterator<Result> iterator = suggests.iterator();
        while (iterator.hasNext()) {
            Result next = iterator.next();
            System.out.println(next.getFileName());
            System.out.println(next);
        }
        return suggests;
    }

}
