package com.gdei.searchengine.service;

import com.chenlb.mmseg4j.analysis.ComplexAnalyzer;
import com.chenlb.mmseg4j.analysis.MMSegAnalyzer;
import com.chenlb.mmseg4j.analysis.SimpleAnalyzer;
import com.gdei.searchengine.core.Searcher;
import com.gdei.searchengine.domain.Result;
import com.gdei.searchengine.domain.ResultIterator;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.suggest.Lookup;
import org.apache.lucene.search.suggest.analyzing.AnalyzingInfixSuggester;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
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
    public List<Result> pageSearch(String indexDir, String parameter, int page) throws Exception {
        List<Result> results = searcher.pageSearch(indexDir, parameter, page);
        return results;
    }

    @Override
    public List<Result> booleanSearch(String indexDir, String parameter, int page) throws Exception {
        List<Result> results = searcher.booleanSearch(indexDir, parameter, page);
        return results;
    }

    @Override
    public List<Result>  suggestSearch(String key) throws Exception {
        AnalyzingInfixSuggester suggester = null;
        List<Result> suggestResults = null;
        try {

//            Directory indexDir = FSDirectory.open(Paths.get(IndexServiceImpl.indexDirectory));

            //创建一个内存索引库
            RAMDirectory indexDir = new RAMDirectory();
//            Analyzer analyzer = new ComplexAnalyzer();

            Analyzer analyzer = new MMSegAnalyzer() ; //分词器

            //AnalyzingInfixSuggester，关键词联想的核心类
            suggester = new AnalyzingInfixSuggester(indexDir, analyzer, analyzer, 2, false);

            //返回所有数据，然后进行查询
            List<Result> results = searchAllFile();

            suggester.build(new ResultIterator(results.iterator()));
            List<Lookup.LookupResult> lookupResults = suggester.lookup(key, 5, false, false);

            suggestResults = new ArrayList<>();
            for (Lookup.LookupResult result : lookupResults) {
//                System.out.println(result.key);
                BytesRef bytesRef = result.payload;
                ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(bytesRef.bytes));
                Result suggestResult = null;
                try {
                    suggestResult = (Result) is.readObject();
//                    suggestResults.add(suggestResult.getFileName());
                    suggestResults.add(suggestResult);
                } catch (ClassNotFoundException e) {

                    e.printStackTrace();
                }
            }

            return suggestResults;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            suggester.refresh();
        }


        return suggestResults;
    }

    /**
     *
     * 返回索引库中所有Document
     * @return
     */
    @Override
    public List<Result> searchAllFile() {
        Directory directory = null;
        IndexReader indexReader = null;
        try {
            //把索引库加载到内存中，对应Directory对象
            directory = FSDirectory.open(Paths.get(IndexServiceImpl.indexDirectory));

            //IndexReader对象来读取内存中的索引库，即Directory对象
            indexReader = DirectoryReader.open(directory);

            //创建中文分词器,这里分别使用了SmartChineseAnalyzer、mmseg4j的ComplexAnalyzer
            //SmartChineseAnalyzer analyzer = new SmartChineseAnalyzer();
            Analyzer analyzer = new ComplexAnalyzer();

            //创建索引查询器
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);

            int count = indexReader.maxDoc();//所有文档数
            List<Result> results = new ArrayList<>();
            System.out.println("count总数是：" + count);
            for (int i = 0; i < count; i++) {
                Document doc = indexSearcher.doc(i);//根据docID拿到索引库中的Document对象
                String fileName = doc.get("fileName");
                //不显示文件名后缀
                String newfileName = fileName.substring(0,fileName.lastIndexOf("."));
                Result result = new Result();
                result.setFileName(newfileName);
                results.add(result);
            }
            indexReader.close();
            directory.close();

            return results;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
