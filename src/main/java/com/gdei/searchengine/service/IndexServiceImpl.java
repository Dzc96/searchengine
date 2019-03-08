package com.gdei.searchengine.service;

import com.gdei.searchengine.core.Indexer;
import org.springframework.stereotype.Service;

@Service
public class IndexServiceImpl implements IndexService{
    /**
     *   String dataDirectory = "D:\\LuceneDemo01";
     *         String indexDirectory = "D:\\LuceneDemo\\index_demo_bysj";
     *
     *         Indexer indexer = new Indexer();
     *         indexer.index(dataDirectory, indexDirectory);
     *         System.out.println("一共对" + number + "个文件创建了索引。");
     */
    public void createIndex(String dataDirectory) throws Exception{
        //默认在D盘下创建索引库
        String indexDirectory = "D:\\index_directory";
        Indexer indexer = new Indexer();
        indexer.index(dataDirectory, indexDirectory);
    }
}
