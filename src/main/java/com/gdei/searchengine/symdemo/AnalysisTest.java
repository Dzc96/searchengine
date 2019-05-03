package com.gdei.searchengine.symdemo;

import java.io.IOException;
import java.io.StringReader;
 
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.junit.Test;
 
public class AnalysisTest {
	@Test
	public void tokenTest() {
		Analyzer analyzer = new StandardAnalyzer(); // or any other analyzer
		TokenStream ts = null;
		try {
			ts = analyzer.tokenStream("myfield", new StringReader(
					"some text goes here"));
			OffsetAttribute offsetAtt = ts.addAttribute(OffsetAttribute.class);
			ts.reset(); // Resets this stream to the beginning. (Required)
			while (ts.incrementToken()) {
				// Use AttributeSource.reflectAsString(boolean)
				// for token stream debugging.
				System.out.println("token: " + ts.reflectAsString(true));
 
				System.out.println("token start offset: "
						+ offsetAtt.startOffset());
				System.out.println("token end offset: "
						+ offsetAtt.endOffset());
			}
			ts.end(); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				ts.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
 
}

