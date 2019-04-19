package com.gdei.searchengine.core;

import com.chenlb.mmseg4j.analysis.ComplexAnalyzer;
import com.gdei.searchengine.domain.Result;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.nio.file.Paths;
import java.util.ArrayList;

@Component
public class Searcher {
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
//        String[] fields = {"fileName", "content"};
        QueryParser parser = new QueryParser("contents", analyzer);

        //多域查询
//        String[] queries = {parameter, parameter};
//
//        String[] fields = {"fileName", "content"};
//
//        BooleanClause.Occur[] clauses = { BooleanClause.Occur.SHOULD, BooleanClause.Occur.SHOULD };
//
//        Query query = MultiFieldQueryParser.parse(queries, fields, clauses, new ComplexAnalyzer());

//        QueryParser parser = new MultiFieldQueryParser(fields, analyzer);




        //根据传进来的参数构建Query对象
        Query query = parser.parse(parameter);

        long start = System.currentTimeMillis();
        TopDocs topDocs = indexSearcher.search(query, 10);
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

//            TokenStream fileNameTokenStream=analyzer.tokenStream("fileName", new StringReader(fileName));
//            String bestFilename = highlighter.getBestFragment(fileNameTokenStream, fileName);

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



}
