package com.gdei.searchengine.service;

import org.springframework.stereotype.Service;


public interface IndexService {
     void createIndex(String dataDirectory) throws Exception;
}
