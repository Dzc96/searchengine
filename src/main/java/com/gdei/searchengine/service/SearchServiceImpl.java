package com.gdei.searchengine.service;

import com.gdei.searchengine.core.Searcher;
import com.gdei.searchengine.domain.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    Searcher searcher;


    /**
     * 应该返回一个查询结果 List<某对象>，还要写一个类封装文件的查询结果
     * @param indexDir
     * @param parameter
     * @throws Exception
     */
    public ArrayList<Result> search(String indexDir, String parameter) throws Exception{
        return searcher.search(indexDir, parameter);
    }

    @Override
    public ArrayList<Result> pageSearch(String indexDir, String parameter, int page) throws Exception {
        ArrayList<Result> results = searcher.pageSearch(indexDir, parameter, page);
        return results;
    }
}
