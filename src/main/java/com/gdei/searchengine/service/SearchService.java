package com.gdei.searchengine.service;

import com.gdei.searchengine.domain.Result;
import org.apache.lucene.search.suggest.analyzing.AnalyzingInfixSuggester;

import java.util.ArrayList;

public interface SearchService {
    public ArrayList<Result> search(String indexDir, String parameter) throws Exception;

    public ArrayList<Result> pageSearch(String indexDir, String parameter, int page) throws Exception;

    public ArrayList<Result> booleanSearch(String indexDir, String parameter, int page) throws Exception;

    public void testSuggest(AnalyzingInfixSuggester suggester, String fileName) throws Exception;
}
