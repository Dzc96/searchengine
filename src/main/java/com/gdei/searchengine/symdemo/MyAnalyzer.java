package com.gdei.searchengine.symdemo;

import java.io.IOException;
 
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseTokenizer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

 
public class MyAnalyzer extends Analyzer {
	private CharArraySet stopWordSet;//停止词词典

	public CharArraySet getStopWordSet() {
		return stopWordSet;
	}

	public void setStopWordSet(CharArraySet stopWordSet) {
		this.stopWordSet = stopWordSet;
	}


	public MyAnalyzer() {
		super();
		this.stopWordSet = StopAnalyzer.ENGLISH_STOP_WORDS_SET;//可在此基础上拓展停止词
	}

	/**
	 * 扩展停止词
	 *
	 * @param stops
	 */
	public MyAnalyzer(String[] stops) {
		this();
		stopWordSet.addAll(StopFilter.makeStopSet(stops));
	}

	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		//正则匹配分词
		Tokenizer source = new LowerCaseTokenizer();
		return new TokenStreamComponents(source, new StopFilter(source, stopWordSet));
	}


















	public static void main(String[] args) {
		Analyzer analyzer = new MyAnalyzer();
		String words = "A AN yuyu";
		TokenStream stream = null;

		try {
			stream = analyzer.tokenStream("myfield", words);
			stream.reset();
			CharTermAttribute offsetAtt = stream.addAttribute(CharTermAttribute.class);
			while (stream.incrementToken()) {
				System.out.println(offsetAtt.toString());
			}
			stream.end();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}

