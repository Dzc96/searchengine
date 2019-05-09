package com.gdei.searchengine;

import com.chenlb.mmseg4j.analysis.ComplexAnalyzer;
import com.chenlb.mmseg4j.analysis.MMSegAnalyzer;
import com.chenlb.mmseg4j.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Test;
import org.springframework.stereotype.Component;

import java.io.StringReader;

public class AnalyzerStudy extends SearchengineApplicationTests{

    @Test
    public void test1() throws  Exception{
//需要处理的测试字符串-->选择一个有趣的字符串即可
//        String str = "这是一个刁志聪的分词器测试程序，希望大家继续关注我的个人系列博客：基于Lucene的案例开发，这里加一点带空格的标签 LUCENE java 分词器";
//        String str = "广东第二师范学院（Guangdong University of Education）其前身为广东教育学院创建于1955年，2010年3月，经国家教育部批准，改为广东第二师范学院，是广东省属的普通本科学校。2014年，获得本科学位授予权。";
//        String str = "经中国教育部批准，广东教育学院于2010年正式改名为广东第二师范学院，是广东省属普通本科院校。";
        String str = "本人就读于广东第二师范学院的计算机科学系软件工程专业";
        Analyzer analyzer = null;
        //标准分词器，如果用来处理中文，和ChineseAnalyzer有一样的效果，这也许就是之后的版本弃用ChineseAnalyzer的一个原因
        analyzer = new StandardAnalyzer();
        //第三方中文分词器，有下面2中构造方法。
//        analyzer = new IKAnalyzer();
//        analyzer = new IKAnalyzer(false);
//        analyzer = new IKAnalyzer(true);
        //空格分词器，对字符串不做如何处理
//        analyzer = new WhitespaceAnalyzer();
        //简单分词器，一段一段话进行分词
        analyzer = new SimpleAnalyzer();
        //刁志聪|就读|于|广东第二师范学院|计算机科学|系|软件工程|专业|
        //二分法分词器，这个分词方式是正向退一分词(二分法分词)，同一个字会和它的左边和右边组合成一个次，每个人出现两次，除了首字和末字
//        analyzer = new CJKAnalyzer();
        //关键字分词器，把处理的字符串当作一个整体
//        analyzer = new KeywordAnalyzer();
        //被忽略的词分词器
//        analyzer = new StopAnalyzer();

        analyzer = new ComplexAnalyzer();
        //刁志聪|就读|于|广东第二师范学院|计算机科学|系|软件工程|专业|


//        analyzer = new MMSegAnalyzer();
        //刁|志|聪|就读|于|广东|第二|师范|学院|计算|机|科学|系|软件|工程|专业|

        //使用分词器处理测试字符串
        StringReader reader = new StringReader(str);
        TokenStream tokenStream  = analyzer.tokenStream("", reader);
        tokenStream.reset();
        CharTermAttribute term = tokenStream.getAttribute(CharTermAttribute.class);
        int l = 0;
        //输出分词器和处理结果
        System.out.println(analyzer.getClass());
        System.out.println();
        System.out.println();
        System.out.println("分词效果：");
        while(tokenStream.incrementToken()){
            System.out.print(term.toString() + "|");
            l += term.toString().length();
            //如果一行输出的字数大于30，就换行输出
            if (l > 30) {
                System.out.println();
                l = 0;
            }
        }

        System.out.println();
    }

}
