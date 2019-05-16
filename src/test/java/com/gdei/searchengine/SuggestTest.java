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


//    @Test
////    public void test1() throws Exception {
////        List<Result> results = searchService.suggestSearchByTrie("广东");
////        Iterator<Result> iterator = results.iterator();
////        while (iterator.hasNext()) {
////            Result result = iterator.next();
////            System.out.println(result.getFileName());
////        }
////    }
}
