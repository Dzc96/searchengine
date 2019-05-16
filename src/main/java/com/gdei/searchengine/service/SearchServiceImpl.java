package com.gdei.searchengine.service;

import com.gdei.searchengine.core.Searcher;
import com.gdei.searchengine.domain.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    Searcher searcher;

    /**
     * 应该返回一个查询结果 List<某对象>，还要写一个类封装文件的查询结果
     *
     * @param indexDir
     * @param parameter
     * @throws Exception
     */
    public List<Result> search(String indexDir, String parameter) throws Exception {
        return searcher.search(indexDir, parameter);
    }


    @Override
    public List<Result> booleanSearch(String indexDir, String parameter, int page) throws Exception {
        List<Result> results = searcher.booleanSearch(indexDir, parameter, page);
        return results;
    }


    @Override
    public List<Result> suggestSearchByTrie(String key) throws Exception {
        List<Result> results = searcher.suggestSearchByTrie(key);
        return results;
    }


}
