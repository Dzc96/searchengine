package com.gdei.searchengine.core;


import com.chenlb.mmseg4j.analysis.ComplexAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xslf.extractor.XSLFPowerPointExtractor;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Paths;

/**
 * 主要是构建基于中文的索引库，同时支持对PDF、Word、Excel等文件进行解析创建索引，并对这些文件进行查询
 * 一般来说，不需要对Document的内容进行存储，但要分词和创建索引
 */
@Component
public class Indexer {

    //根据索引库对应的Directory对象和mmseg4j中文分词器，获得IndexWriter对象
    public IndexWriter getWriter(Directory directory) throws Exception {
        //mmseg4j
        Analyzer analyzer = new ComplexAnalyzer();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        return new IndexWriter(directory, indexWriterConfig);
    }

    //索引文档总数
    static int number = 0;


    /**
     * 对各种类型的文档创建索引
     *
     * @throws Exception
     */
    @SuppressWarnings("Duplicates")
    public void index(String dataDirectory, String indexDirectory) throws Exception {

        Directory directory = FSDirectory.open(Paths.get(indexDirectory));

        //拿到对应文件夹的所有文件, 用递归处理所有目录
        File[] files = new File(dataDirectory).listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory() && !files[i].isHidden()) {
                index(files[i].getCanonicalFile().toString(), indexDirectory);
            } else if (files[i].getName().indexOf(".") != -1 && !files[i].isHidden()) {
                //根据索引库获得操作索引库的IndexWriter
                IndexWriter indexWriter = getWriter(directory);
                // 获取文件名称
                String fileName = files[i].getName();
                // 获取文件后缀名，将其作为文件类型
                String fileType = fileName.substring(fileName.lastIndexOf(".") + 1,
                        fileName.length()).toLowerCase();
                Document doc = new Document();
                InputStream in = new FileInputStream(files[i]);
                number++;
                //根据文件类型使用相应的代码创建索引
                if (fileType != null && !fileType.equals("")) {
                    if (fileType.equals("doc")) {
                        // 获取doc的word文档
                        WordExtractor wordExtractor = new WordExtractor(in);
                        doc.add(new TextField("fileName", files[i].getName(), Field.Store.YES));
                        doc.add(new TextField("contents", wordExtractor.getText(), Field.Store.YES));
                        doc.add(new StringField("fullPath", files[i].getCanonicalPath(), Field.Store.YES));
                        indexWriter.addDocument(doc);
                    } else if (fileType.equals("docx")) {
                        XWPFWordExtractor xwpfWordExtractor = new XWPFWordExtractor(
                                new XWPFDocument(in));
                        doc.add(new TextField("fileName", files[i].getName(), Field.Store.YES));
                        doc.add(new TextField("contents", xwpfWordExtractor.getText(), Field.Store.YES));
                        doc.add(new StringField("fullPath", files[i].getCanonicalPath(), Field.Store.YES));
                        xwpfWordExtractor.close();
                        indexWriter.addDocument(doc);
                    } else if (fileType.equals("pdf")) {
                        PDFParser parser = new PDFParser(in);
                        parser.parse();
                        PDDocument pdDocument = parser.getPDDocument();
                        PDFTextStripper stripper = new PDFTextStripper();
                        doc.add(new TextField("fileName", files[i].getName(), Field.Store.YES));
                        doc.add(new TextField("contents", stripper.getText(pdDocument), Field.Store.YES));
                        doc.add(new StringField("fullPath", files[i].getCanonicalPath(), Field.Store.YES));
                        indexWriter.addDocument(doc);
                        pdDocument.close();
                    } else if (fileType.equals("txt")) {
                        InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(new File(files[i].getCanonicalPath())), "GBK");
                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                        StringBuilder result = new StringBuilder();
                        String lineTxt = null;
                        while ((lineTxt = bufferedReader.readLine()) != null) {
                            result.append(lineTxt);
                        }
                        bufferedReader.close();
                        inputStreamReader.close();
                        doc.add(new TextField("fileName", files[i].getName(), Field.Store.YES));
                        doc.add(new TextField("contents", result.toString(), Field.Store.YES));
                        doc.add(new StringField("fullPath", files[i].getCanonicalPath(), Field.Store.YES));
                        indexWriter.addDocument(doc);
                    } else if (fileType.equals("xls")) {
                        InputStream is = null;
                        HSSFWorkbook wb = null;
                        String text = "";
                        is = new FileInputStream(files[i].toString());
                        wb = new HSSFWorkbook(new POIFSFileSystem(is));
                        ExcelExtractor extractor = new ExcelExtractor(wb);
                        extractor.setFormulasNotResults(false);
                        extractor.setIncludeSheetNames(true);
                        text = extractor.getText();
                        extractor.close();
                        doc.add(new TextField("fileName", files[i].getName(), Field.Store.YES));
                        doc.add(new TextField("contents", text, Field.Store.YES));
                        doc.add(new StringField("fullPath", files[i].getCanonicalPath(), Field.Store.YES));
                        indexWriter.addDocument(doc);
                    } else if (fileType.equals("xlsx")) {
                        InputStream is = null;
                        XSSFWorkbook workBook = null;
                        String text = "";
                        is = new FileInputStream(files[i].toString());
                        workBook = new XSSFWorkbook(is);
                        XSSFExcelExtractor extractor = new XSSFExcelExtractor(workBook);
                        text = extractor.getText();
                        doc.add(new TextField("fileName", files[i].getName(), Field.Store.YES));
                        doc.add(new TextField("contents", text, Field.Store.YES));
                        doc.add(new StringField("fullPath", files[i].getCanonicalPath(), Field.Store.YES));
                        indexWriter.addDocument(doc);
                        extractor.close();
                    } else if (fileType.equals("pptx")) {
                        InputStream is = null;
                        XMLSlideShow slide = null;
                        String text = "";
                        is = new FileInputStream(files[i].toString());
                        slide = new XMLSlideShow(is);
                        XSLFPowerPointExtractor extractor = new XSLFPowerPointExtractor(slide);
                        text = extractor.getText();
                        doc.add(new TextField("fileName", files[i].getName(), Field.Store.YES));
                        doc.add(new TextField("contents", text, Field.Store.YES));
                        doc.add(new StringField("fullPath", files[i].getCanonicalPath(), Field.Store.YES));
                        extractor.close();
                    }
                }

                indexWriter.close();
            }
        }

        //对新文档创建索引后，维护用于自动补全的字典树
        Searcher.results = Searcher.searchAllFile(); //用HashMap在内存中保存拼音和文件名的对应关系
        Searcher.trie = Searcher.getTrie(Searcher.results);
        Searcher.abbrTrie = Searcher.getAbbrTrie(Searcher.results);

        //for循环结束
    }

    public static void main(String[] args) throws Exception {

        String dataDirectory = "D:\\LuceneDemo01";
        String indexDirectory = "D:\\LuceneDemo\\index_demo_bysj";

        Indexer indexer = new Indexer();
        indexer.index(dataDirectory, indexDirectory);
        System.out.println("一共对" + number + "个文件创建了索引。");
    }


}
