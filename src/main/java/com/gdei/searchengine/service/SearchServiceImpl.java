package com.gdei.searchengine.service;

import com.gdei.searchengine.core.Searcher;
import com.gdei.searchengine.domain.Result;
import org.apache.lucene.search.suggest.Lookup;
import org.apache.lucene.search.suggest.analyzing.AnalyzingInfixSuggester;
import org.apache.lucene.util.BytesRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    Searcher searcher;


    /**
     * 应该返回一个查询结果 List<某对象>，还要写一个类封装文件的查询结果
     * @param indexDir
     * @param parameter
     * @throws Exception
     */
    public ArrayList<Result> search(String indexDir, String parameter) throws Exception{
        return searcher.search(indexDir, parameter);
    }

    @Override
    public ArrayList<Result> pageSearch(String indexDir, String parameter, int page) throws Exception {
        ArrayList<Result> results = searcher.pageSearch(indexDir, parameter, page);
        return results;
    }

    @Override
    public ArrayList<Result> booleanSearch(String indexDir, String parameter, int page) throws Exception {
        ArrayList<Result> results = searcher.booleanSearch(indexDir, parameter, page);
        return results;
    }

    @Override
    public void testSuggest(AnalyzingInfixSuggester suggester, String fileName) throws Exception{
        HashSet<BytesRef> contexts = new HashSet<BytesRef>();
        contexts.add(new BytesRef(fileName.getBytes("UTF8")));
        List<Lookup.LookupResult> results = suggester.lookup(fileName, contexts, 2, true, false);

        for (Lookup.LookupResult result : results) {
            System.out.println(result.key);//result.key中存储的是根据用户输入内部算法进行匹配后返回的suggest内容
            //从载荷（payload）中反序列化出Result对象(实际生产中出于降低内存占用考虑一般不会在载荷中存储这么多内容)
            BytesRef bytesRef = result.payload;
            ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(bytesRef.bytes));
            Result resultOne = null;
            try {
                resultOne = (Result)is.readObject();
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.out.println("fileName" + resultOne.getFileName());
            System.out.println("contents:" + resultOne.getHighlighterFragment());
            System.out.println("fullPath" + resultOne.getFullPath());

        }
        System.out.println();



    }
}
