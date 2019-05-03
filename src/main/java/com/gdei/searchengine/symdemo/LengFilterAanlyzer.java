package com.gdei.searchengine.symdemo;

import java.io.IOException;
 
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseTokenizer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.miscellaneous.LengthFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

 
public class LengFilterAanlyzer extends Analyzer {
	private int len;

	public int getLen() {
		return len;
	}


	public void setLen(int len) {
		this.len = len;
	}


	public LengFilterAanlyzer() {
		super();
	}


	public LengFilterAanlyzer(int len) {
		super();
		this.len = len;
	}


	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		final Tokenizer source = new WhitespaceTokenizer();
		TokenStream result = new LengthFilter(source, len, Integer.MAX_VALUE);
		return new TokenStreamComponents(source, result);

	}

	public static void main(String[] args) {
		Analyzer analyzer = new LengFilterAanlyzer(2);
		String words = "I am a java coder";
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