package com.gdei.searchengine;

import com.gdei.searchengine.domain.Result;
import com.gdei.searchengine.service.SearchService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Iterator;
import java.util.List;

public class SuggestTest extends SearchengineApplicationTests{
    @Autowired
    SearchService searchService;


    //这个测试方法有问题
    @Test
    public void test1() throws Exception{
        List<Result> results = searchService.suggestSearch("教育");
        System.out.println("results的大小：" + results.size());
//        Iterator<Result> iterator = results.iterator();
//
//        while (iterator.hasNext()) {
//            Result next = iterator.next();
//            System.out.println(next.getFileName());
//        }
    }


//    @Test
//    public void test2() throws Exception{
//        List<Result> results = searchService.searchAllFile();
//        System.out.println("document的数量" + results.size());
//        Iterator<Result> iterator = results.iterator();
//        while (iterator.hasNext()) {
//            Result next = iterator.next();
//            System.out.println(next.getFileName());
//        }
//    }
}
