package com.gdei.searchengine.core;

import com.chenlb.mmseg4j.analysis.ComplexAnalyzer;
import com.chenlb.mmseg4j.analysis.MMSegAnalyzer;
import com.gdei.searchengine.domain.Result;
import com.gdei.searchengine.service.IndexServiceImpl;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.search.suggest.Lookup;
import org.apache.lucene.search.suggest.analyzing.AnalyzingInfixSuggester;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.springframework.stereotype.Component;
import org.apache.lucene.search.BooleanQuery;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.*;


@Component
public class Searcher {

    //维护一个文件名和文件名对应拼音的映射
    public static Map<String, String> cn2pinyin = new HashMap<>();
    public static List<Result> results;
    //维护一个拼音树
    public static Trie trie;
    //维护一个拼音缩写树
    public static Trie abbrTrie;


    //每页的记录数量
    public static final int PAGE_SIZE = 5;

    @SuppressWarnings("Duplicates")
    public List<Result> search(String indexDir, String parameter) throws Exception {
        //把索引库加载到内存中，对应Directory对象
        Directory directory = FSDirectory.open(Paths.get(indexDir));

        //IndexReader对象来读取内存中的索引库，即Directory对象
        IndexReader indexReader = DirectoryReader.open(directory);

        //创建索引查询器
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        //创建中文分词器,这里分别使用了SmartChineseAnalyzer、mmseg4j的ComplexAnalyzer
        //SmartChineseAnalyzer analyzer = new SmartChineseAnalyzer();
        Analyzer analyzer = new ComplexAnalyzer();

        //建立查询解析器，先分词再查询
        QueryParser parser = new QueryParser("contents", analyzer);



        //根据传进来的参数构建Query对象
//        Query query = parser.parse(parameter);
        Query query = parser.parse(QueryParser.escape(parameter));


        long start = System.currentTimeMillis();
        //默认查询十条
        TopDocs topDocs = indexSearcher.search(query, 100);
        //命中的Document总数
        long totalNumber = topDocs.totalHits;

        long end = System.currentTimeMillis();
        System.out.println("匹配 ["+parameter+ "],总共花费了"+(end-start)+"毫秒,共查到"+totalNumber+"条记录。");


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
            if (!contents.isEmpty()) {
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



    //通过布尔查询实现多域查询，即通过传入的查询参数，对文件名fileName域、文件内容contents域进行
    @SuppressWarnings("Duplicates")
    public List<Result> booleanSearch(String indexDir, String parameter, int page) throws Exception {
        //把索引库加载到内存中，对应Directory对象
        Directory directory = FSDirectory.open(Paths.get(indexDir));

        //IndexReader对象来读取内存中的索引库，即Directory对象
        IndexReader indexReader = DirectoryReader.open(directory);

        //创建索引查询器
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        //创建中文分词器,这里分别使用了SmartChineseAnalyzer、mmseg4j的ComplexAnalyzer
        //SmartChineseAnalyzer analyzer = new SmartChineseAnalyzer();
        Analyzer analyzer = new ComplexAnalyzer();
//        Analyzer analyzer = new ComplexAnalyzer("data/*.dic");
        //指定要查询的域
        String[] fields  = {"contents", "fileName"};
        BooleanQuery.Builder builder = new BooleanQuery.Builder();

        //在contents和fileName域上都进行同一个关键词的查找，查询时先对关键词进行分词
        //两个Query的查询都为SHOULD，意味着求它们各自查询结果的并集，也就是说文件名和文件内容中出现了关键字的文档对象都可以查出来
        for (int i = 0; i < fields.length; i++) {
            QueryParser queryParser = new QueryParser(fields[i], analyzer);
            Query query = queryParser.parse(parameter);
            builder.add(query,BooleanClause.Occur.SHOULD);
        }


        BooleanQuery booleanQuery = builder.build();//先拿到Builder-->builder.build()-->BooleanQuery
        TopDocs topDocs = indexSearcher.search(booleanQuery, 100);
        Long totalNumber = topDocs.totalHits;



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
        int start = (page - 1) * PAGE_SIZE;
        int end = page * PAGE_SIZE;
        int endNumber = end;

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
            if (!contents.isEmpty()) {
                TokenStream tokenStream2=analyzer.tokenStream("contents", new StringReader(contents));
                String highlighterFragment = highlighter.getBestFragment(tokenStream2, contents) + "...";
                result.setHighlighterFragment(highlighterFragment);
                results.add(result);
            }
        }
        indexReader.close();
        return results;
    }


    /**
     * 返回索引库中所有Document对象
     *
     * @return
     */
    public static  List<Result> searchAllFile() {
        Directory directory = null;
        IndexReader indexReader = null;
        try {
            //把索引库加载到内存中，对应Directory对象
            directory = FSDirectory.open(Paths.get(IndexServiceImpl.indexDirectory));

            //IndexReader对象来读取内存中的索引库，即Directory对象
            indexReader = DirectoryReader.open(directory);

            //创建中文分词器,这里分别使用了SmartChineseAnalyzer、mmseg4j的ComplexAnalyzer
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
                String newfileName = fileName.substring(0, fileName.lastIndexOf("."));
                Result result = new Result();
                result.setFileName(newfileName);
                results.add(result);
                //建立拼音和文件名的映射关系，建立拼音缩写和文件名的映射关系
                cn2pinyin.put(PinYin.getPinYin(newfileName), newfileName);
                cn2pinyin.put(PinYin.getPinYinHeadChar(newfileName), newfileName);
            }

            indexReader.close();
            directory.close();
            return results;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    public  List<Result> suggestSearchByTrie(String key) throws Exception {
        System.out.println("传入的这个key是:" + key);


        //preword传过来之后要作处理，有中文就过滤出来转成拼音
        //根据前缀模糊查找Trie字典树，先查找拼音缩写树，有就直接返回，没有就继续查拼音树
        String headerWord = PinYin.getPinYinHeadChar(key);
        List<String> datas = abbrTrie.getData(headerWord);
        if (datas != null) { //说明拼音缩写树中有数据
            //保存中文结果
            List<Result> cnResults = new ArrayList<>();
            Iterator<String> stringIterator = datas.iterator();
            while (stringIterator.hasNext()) {
                String py = stringIterator.next();
                String fileName = Searcher.cn2pinyin.get(py);
                Result result = new Result();
                result.setFileName(fileName);
                cnResults.add(result);
            }
            System.out.println(cnResults);
            return cnResults;

        } else { //查找拼音树
            String preword = PinYin.getPinYin(key);
            datas = trie.getData(preword); //datas为null了,我感觉是我的树没构建好
            if (datas == null) {
                System.out.println("datas为null，根据前缀查询没有数据");
                System.out.println("本次搜索的关键字是:" + key);
                return null;
            }
            Iterator<String> stringIterator = datas.iterator();

            //保存中文结果，这个地方建议提取成一个方法
            List<Result> cnResults = new ArrayList<>();
            while (stringIterator.hasNext()) {
                String py = stringIterator.next();
                String fileName = Searcher.cn2pinyin.get(py);
                Result result = new Result();
                result.setFileName(fileName);
                cnResults.add(result);
            }
            System.out.println(cnResults);
            return cnResults;
        }

    }


    //构建基于拼音的字典树
    public static Trie getTrie(List<Result> results) {
        Trie trie = new Trie();

        Iterator<Result> iterator = results.iterator();
        while (iterator.hasNext()) {
            Result result = iterator.next();
            trie.insert(PinYin.getPinYin(result.getFileName()));
        }
        return trie;
    }

    //构建基于拼音缩写的字典树
    public static Trie getAbbrTrie(List<Result> results) {
        Trie trie = new Trie();

        Iterator<Result> iterator = results.iterator();
        while (iterator.hasNext()) {
            Result result = iterator.next();
            trie.insert(PinYin.getPinYinHeadChar(result.getFileName()));
        }
        return trie;
    }







}
