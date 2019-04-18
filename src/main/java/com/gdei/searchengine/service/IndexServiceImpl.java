package com.gdei.searchengine.service;

import com.gdei.searchengine.core.Indexer;
import org.springframework.stereotype.Service;

@Service
public class IndexServiceImpl implements IndexService{

    //这里不应该写死，应该弹出一个框让人选择
    public static String indexDirectory = "C:\\Users\\EDZ\\Desktop\\index_directory";

    /**
     *   String dataDirectory = "D:\\LuceneDemo01";
     *         String indexDirectory = "D:\\LuceneDemo\\index_demo_bysj";
     *
     *         Indexer indexer = new Indexer();
     *         indexer.index(dataDirectory, indexDirectory);
     *         System.out.println("一共对" + number + "个文件创建了索引。");
     */
    public void createIndex(String dataDirectory) throws Exception{
        Indexer indexer = new Indexer();
        indexer.index(dataDirectory, IndexServiceImpl.indexDirectory);
    }
}
