package com.gdei.searchengine.service;

import com.chenlb.mmseg4j.analysis.ComplexAnalyzer;
import com.chenlb.mmseg4j.analysis.MMSegAnalyzer;
import com.gdei.searchengine.core.Searcher;
import com.gdei.searchengine.domain.Result;
import com.gdei.searchengine.domain.ResultIterator;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.suggest.Lookup;
import org.apache.lucene.search.suggest.analyzing.AnalyzingInfixSuggester;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.lucene.document.Document;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Paths;
import java.util.ArrayList;

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

//    @Override
//    public List<Result> pageSearch(String indexDir, String parameter, int page) throws Exception {
//        List<Result> results = searcher.pageSearch(indexDir, parameter, page);
//        return results;
//    }

    @Override
    public List<Result> booleanSearch(String indexDir, String parameter, int page) throws Exception {
        List<Result> results = searcher.booleanSearch(indexDir, parameter, page);
        return results;
    }

    @Override
    public List<Result> suggestSearch(String key) throws Exception {
        List<Result> results = searcher.suggestSearch(key);
        return results;
    }



}
