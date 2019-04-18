package com.gdei.searchengine.service;

import com.gdei.searchengine.core.Searcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public void search(String indexDir, String parameter) throws Exception{
        searcher.search(indexDir, parameter);
    }
}
