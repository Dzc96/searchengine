package com.gdei.searchengine.service;

import com.gdei.searchengine.domain.Result;

import java.util.ArrayList;

public interface SearchService {
    public ArrayList<Result> search(String indexDir, String parameter) throws Exception;

    public ArrayList<Result> pageSearch(String indexDir, String parameter, int page) throws Exception;

}
