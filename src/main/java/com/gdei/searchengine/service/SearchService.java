package com.gdei.searchengine.service;

import com.gdei.searchengine.domain.Result;
import org.apache.lucene.search.suggest.analyzing.AnalyzingInfixSuggester;

import java.util.List;

public interface SearchService {
    List<Result> search(String indexDir, String parameter) throws Exception;

    List<Result> booleanSearch(String indexDir, String parameter, int page) throws Exception;

    List<Result> suggestSearchByTrie(String key) throws Exception;

}
