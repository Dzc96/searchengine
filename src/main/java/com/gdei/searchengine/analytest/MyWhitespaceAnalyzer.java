package com.gdei.searchengine.analytest;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;


/**
 * 5. 建立分析器
 * @author THINKPAD
 *
 */
public class MyWhitespaceAnalyzer extends Analyzer {

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        //既有Tokenizer，也有TokenStream
        Tokenizer source = new MyWhitespaceTokenizer();
        TokenStream filter = new MyLowerCaseTokenFilter(source);
        return new TokenStreamComponents(source, filter);
    }

    public static void main(String[] args) {

        String text = "An AttributeSource contains a list of different AttributeImpls, and methods to add and get them. ";

        try {
            Analyzer ana = new MyWhitespaceAnalyzer();
            TokenStream ts = ana.tokenStream("aa", text);
            MyCharAttribute ca = ts.getAttribute(MyCharAttribute.class);
            ts.reset();
            while (ts.incrementToken()) {
                System.out.print(ca.getString() + "  |  ");
//                System.out.println(ca.getString());


            }
            ts.end();
            ana.close();
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
