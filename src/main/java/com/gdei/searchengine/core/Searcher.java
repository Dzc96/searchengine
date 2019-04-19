package com.gdei.searchengine.core;

import com.chenlb.mmseg4j.analysis.ComplexAnalyzer;
import com.gdei.searchengine.domain.Result;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.xml.builders.BooleanQueryBuilder;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Component;
import org.apache.lucene.search.BooleanQuery;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.ArrayList;

@Component
public class Searcher {

    //每页的记录数量
    public static int pageSize = 5;

    public ArrayList<Result> search(String indexDir, String parameter) throws Exception {
        //把索引库加载到内存中，对应Directory对象
        Directory directory = FSDirectory.open(Paths.get(indexDir));

        //IndexReader对象来读取内存中的索引库，即Directory对象
        IndexReader indexReader = DirectoryReader.open(directory);

        //创建索引查询器
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        //创建中文分词器,这里分别使用了SmartChineseAnalyzer、mmseg4j的ComplexAnalyzer
        //SmartChineseAnalyzer analyzer = new SmartChineseAnalyzer();
        Analyzer analyzer = new ComplexAnalyzer();

        //建立查询解析器
        QueryParser parser = new QueryParser("contents", analyzer);



        //根据传进来的参数构建Query对象
        Query query = parser.parse(parameter);

        long start = System.currentTimeMillis();
        //默认查询十条
        TopDocs topDocs = indexSearcher.search(query, 100);
        //命中的Document总数
        Long totalNumber = topDocs.totalHits;

        long end = System.currentTimeMillis();
        System.out.println("匹配 ["+parameter+ "],总共花费了"+(end-start)+"毫秒,共查到"+topDocs.totalHits+"条记录。");


        /*高亮显示开始   */
        //算分
        QueryScorer scorer = new QueryScorer(query);
        //显示得分高的片段
        Fragmenter fragmenter = new SimpleSpanFragmenter(scorer);
        //设置标签内部关键字的颜色
        //第一个参数：标签的前半部分；第二个参数：标签的后半部分。
        SimpleHTMLFormatter simpleHTMLFormatter=new SimpleHTMLFormatter("<b><font color='red'>","</font></b>");

        //第一个参数是对查到的结果进行实例化；第二个是片段得分（显示得分高的片段，即摘要）
        Highlighter highlighter=new Highlighter(simpleHTMLFormatter, scorer);

        //设置片段
        highlighter.setTextFragmenter(fragmenter);
        /*高亮显示结束  */

        //ScoreDoc，描述文档相关度得分和对应文档id的对象
        ArrayList<Result> results = new ArrayList<Result>();
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document document = indexSearcher.doc(scoreDoc.doc);
            String fileName = document.get("fileName");
            String fullPath = document.get("fullPath");
            System.out.println("【文档名字】：" + fileName);
            System.out.println("【文档路径】: " + fullPath);



            String primaryContents = document.get("contents");
            String contents = primaryContents.replace(" ", "");
            if (contents != null) {
                //把得分高的文档的摘要显示出来
                //第一个参数是对哪个参数进行设置；第二个是以流的方式读入
                TokenStream tokenStream=analyzer.tokenStream("contents", new StringReader(contents));
                //获取最高的片段
                System.out.print("【文档摘要内容】：" );
                String highlighterFragment = highlighter.getBestFragment(tokenStream, contents) + "...";
                System.out.println(highlighterFragment);

                //这里写把Document对象变为Result对象返回的代码
                Result result = new Result(fileName, highlighterFragment, fullPath);
                results.add(result);
            }
        }
        indexReader.close();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println("ArrayList中的对象数量:" + results.size());
        System.out.println("------下面是在遍历ArrayList------------");
        for (Result result : results) {
            System.out.println("【文档名字】：" + result.getFileName());
            System.out.println("【文档摘要】: " + result.getHighlighterFragment());
            System.out.println("【文档路径】: " + result.getFullPath());
            System.out.println("-----------结束-----------");
        }
        return results;
    }


    public ArrayList<Result> pageSearch(String indexDir, String parameter, int page) throws Exception {
        //把索引库加载到内存中，对应Directory对象
        Directory directory = FSDirectory.open(Paths.get(indexDir));

        //IndexReader对象来读取内存中的索引库，即Directory对象
        IndexReader indexReader = DirectoryReader.open(directory);

        //创建索引查询器
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        //创建中文分词器,这里分别使用了SmartChineseAnalyzer、mmseg4j的ComplexAnalyzer
        //SmartChineseAnalyzer analyzer = new SmartChineseAnalyzer();
        Analyzer analyzer = new ComplexAnalyzer();

        //建立查询解析器
        QueryParser parser = new QueryParser("contents", analyzer);



        //根据传进来的参数构建Query对象
        Query query = parser.parse(parameter);

        //topDocs就是查询到的记录
        TopDocs topDocs = indexSearcher.search(query, page * pageSize);
        //命中的Document总数
        Long totalNumber = topDocs.totalHits;

        /*高亮显示开始*/
        //算分
        QueryScorer scorer = new QueryScorer(query);
        //显示得分高的片段
        Fragmenter fragmenter = new SimpleSpanFragmenter(scorer);
        //设置标签内部关键字的颜色
        //第一个参数：标签的前半部分；第二个参数：标签的后半部分。
        SimpleHTMLFormatter simpleHTMLFormatter=new SimpleHTMLFormatter("<b><font color='red'>","</font></b>");

        //第一个参数是对查到的结果进行实例化；第二个是片段得分（显示得分高的片段，即摘要）
        Highlighter highlighter=new Highlighter(simpleHTMLFormatter, scorer);

        //设置片段
        highlighter.setTextFragmenter(fragmenter);
        /*高亮显示结束*/

        //ScoreDoc，描述文档相关度得分和对应文档id的对象
        int start = (page - 1) * pageSize;
        int end = page * pageSize;
        Integer endNumber = end;

        if (end > totalNumber) {
            endNumber = Math.toIntExact(totalNumber);
        }

        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        ArrayList<Result> results = new ArrayList<Result>();
        for (int i = start; i < endNumber; i++) {
            Document document = indexSearcher.doc(scoreDocs[i].doc);
            String fileName = document.get("fileName");
            String fullPath = document.get("fullPath");

            String primaryContents = document.get("contents");
            String contents = primaryContents.replace(" ", "");
            if (contents != null) {
                TokenStream tokenStream=analyzer.tokenStream("contents", new StringReader(contents));
                String highlighterFragment = highlighter.getBestFragment(tokenStream, contents) + "...";
                Result result = new Result(fileName, highlighterFragment, fullPath);
                results.add(result);
            }
        }
        indexReader.close();
        return results;
    }



    public ArrayList<Result> booleanSearch(String indexDir, String parameter, int page) throws Exception {
        //把索引库加载到内存中，对应Directory对象
        Directory directory = FSDirectory.open(Paths.get(indexDir));

        //IndexReader对象来读取内存中的索引库，即Directory对象
        IndexReader indexReader = DirectoryReader.open(directory);

        //创建索引查询器
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        //创建中文分词器,这里分别使用了SmartChineseAnalyzer、mmseg4j的ComplexAnalyzer
        //SmartChineseAnalyzer analyzer = new SmartChineseAnalyzer();
        Analyzer analyzer = new ComplexAnalyzer();

        //根据传进来的参数构建Query对象
        String contentField = "contents";
        String fileNameField = "fileName";
        Query query1 = new TermQuery(new Term(contentField, parameter));
        Query query2 = new TermQuery(new Term(fileNameField, parameter));
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        //两个Query的查询都为SHOULD，意味着求它们各自查询结果的并集，也就是说文件名和文件内容中出现了关键字的文档对象都可以查出来
        builder.add(query1, BooleanClause.Occur.SHOULD);
        builder.add(query2, BooleanClause.Occur.SHOULD);

        BooleanQuery booleanQuery = builder.build();//先拿到Builder-->builder.build()-->BooleanQuery
        TopDocs topDocs = indexSearcher.search(booleanQuery, 100);

        Long totalNumber = topDocs.totalHits;
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println("这里是布尔查询,一共匹配了" + totalNumber + "条数据" );


        /*高亮显示开始*/
        //算分
        QueryScorer scorer = new QueryScorer(booleanQuery);
        //显示得分高的片段
        Fragmenter fragmenter = new SimpleSpanFragmenter(scorer);
        //设置标签内部关键字的颜色
        //第一个参数：标签的前半部分；第二个参数：标签的后半部分。
        SimpleHTMLFormatter simpleHTMLFormatter=new SimpleHTMLFormatter("<b><font color='red'>","</font></b>");

        //第一个参数是对查到的结果进行实例化；第二个是片段得分（显示得分高的片段，即摘要）
        Highlighter highlighter=new Highlighter(simpleHTMLFormatter, scorer);

        //设置片段
        highlighter.setTextFragmenter(fragmenter);
        /*高亮显示结束*/

        //ScoreDoc，描述文档相关度得分和对应文档id的对象
        int start = (page - 1) * pageSize;
        int end = page * pageSize;
        Integer endNumber = end;

        if (end > totalNumber) {
            endNumber = Math.toIntExact(totalNumber);
        }

        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        ArrayList<Result> results = new ArrayList<Result>();
        for (int i = start; i < endNumber; i++) {
            Result result = new Result();
            Document document = indexSearcher.doc(scoreDocs[i].doc);
            String fileName = document.get("fileName");
            result.setFileName(fileName);

            //对文件名进行关键字高亮
            TokenStream tokenStream1=analyzer.tokenStream("fileName", new StringReader(fileName));
            String highlighterFileName = highlighter.getBestFragment(tokenStream1, fileName);

            if (highlighterFileName != null) {
                 result.setFileName(highlighterFileName);
            }

            String fullPath = document.get("fullPath");
            result.setFullPath(fullPath);

            String primaryContents = document.get("contents");
            String contents = primaryContents.replace(" ", "");
            if (contents != null) {
                TokenStream tokenStream2=analyzer.tokenStream("contents", new StringReader(contents));
                String highlighterFragment = highlighter.getBestFragment(tokenStream2, contents) + "...";
                result.setHighlighterFragment(highlighterFragment);
                results.add(result);
            }
        }
        indexReader.close();
        return results;
    }



}
