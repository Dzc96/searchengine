package com.gdei.searchengine.service;

import com.gdei.searchengine.core.Indexer;
import com.gdei.searchengine.core.Searcher;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.MergePolicy;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;

@Service
public class IndexServiceImpl implements IndexService{

   
    public static String indexDirectory = "C:\\Users\\EDZ\\Desktop\\index_directory";

    public void createIndex(String dataDirectory) throws Exception{
        Indexer indexer = new Indexer();
        Directory directory = FSDirectory.open(Paths.get(indexDirectory));
        //根据索引库获得操作内存索引库的IndexWriter
        Directory ramDirectory = new RAMDirectory();
        IndexWriter fsWriter = indexer.getFsWriter(directory);
        IndexWriter ramWriter = indexer.getRamWriter(ramDirectory);


        //把创建好的索引放到内存中
        indexer.ramIndexImprove(dataDirectory, indexDirectory, fsWriter, ramWriter);

        //添加索引到磁盘索引库时就强制合并索引，保证索引库中段的数量不超过5
        ramWriter.forceMerge(5);
        ramWriter.close();

        //把内存中的索引添加到磁盘索引库中
        fsWriter.addIndexes(new Directory[]{ramDirectory});
        fsWriter.close();

        //对新文档创建索引后，维护用于自动补全的字典树
        Searcher.results = Searcher.searchAllFile(); //用HashMap在内存中保存拼音和文件名的对应关系
        Searcher.trie = Searcher.getTrie(Searcher.results);
        Searcher.abbrTrie = Searcher.getAbbrTrie(Searcher.results);
    }
}
