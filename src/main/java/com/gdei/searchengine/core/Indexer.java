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
import org.apache.lucene.store.RAMDirectory;
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
import org.junit.Test;
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
    public IndexWriter getFsWriter(Directory directory) throws Exception {
        //mmseg4j
        Analyzer analyzer = new ComplexAnalyzer();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        return new IndexWriter(directory, indexWriterConfig);
    }

    //获得操作内存索引库的IndexWriter
    public IndexWriter getRamWriter(Directory ramDirectory) throws Exception {
        Analyzer analyzer = new ComplexAnalyzer();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        return new IndexWriter(ramDirectory, indexWriterConfig);
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
                IndexWriter indexWriter = getFsWriter(directory);

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
                        indexWriter.addDocument(doc);
                    }
                }

                indexWriter.close();
            }

        }

        //对新文档创建索引后，维护用于自动补全的字典树
        Searcher.results = Searcher.searchAllFile(); //用HashMap在内存中保存拼音和文件名的对应关系
        Searcher.trie = Searcher.getTrie(Searcher.results);
        Searcher.abbrTrie = Searcher.getAbbrTrie(Searcher.results);

    }

    @SuppressWarnings("Duplicates")
    public void indexImprove(String dataDirectory, String indexDirectory, IndexWriter fsWriter) throws Exception {

//        Directory directory = FSDirectory.open(Paths.get(indexDirectory));

        //拿到对应文件夹的所有文件, 用递归处理所有目录
        File[] files = new File(dataDirectory).listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory() && !files[i].isHidden()) {
                indexImprove(files[i].getCanonicalFile().toString(), indexDirectory,fsWriter);
            } else if (files[i].getName().indexOf(".") != -1 && !files[i].isHidden()) {


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
                        fsWriter.addDocument(doc);
                    } else if (fileType.equals("docx")) {
                        XWPFWordExtractor xwpfWordExtractor = new XWPFWordExtractor(
                                new XWPFDocument(in));
                        doc.add(new TextField("fileName", files[i].getName(), Field.Store.YES));
                        doc.add(new TextField("contents", xwpfWordExtractor.getText(), Field.Store.YES));
                        doc.add(new StringField("fullPath", files[i].getCanonicalPath(), Field.Store.YES));
                        xwpfWordExtractor.close();
                        fsWriter.addDocument(doc);
                    } else if (fileType.equals("pdf")) {
                        PDFParser parser = new PDFParser(in);
                        parser.parse();
                        PDDocument pdDocument = parser.getPDDocument();
                        PDFTextStripper stripper = new PDFTextStripper();
                        doc.add(new TextField("fileName", files[i].getName(), Field.Store.YES));
                        doc.add(new TextField("contents", stripper.getText(pdDocument), Field.Store.YES));
                        doc.add(new StringField("fullPath", files[i].getCanonicalPath(), Field.Store.YES));
                        fsWriter.addDocument(doc);
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
                        fsWriter.addDocument(doc);
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
                        fsWriter.addDocument(doc);
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
                        fsWriter.addDocument(doc);
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
            }
        }

        //对新文档创建索引后，维护用于自动补全的字典树
        Searcher.results = Searcher.searchAllFile(); //用HashMap在内存中保存拼音和文件名的对应关系
        Searcher.trie = Searcher.getTrie(Searcher.results);
        Searcher.abbrTrie = Searcher.getAbbrTrie(Searcher.results);

    }


    @SuppressWarnings("Duplicates")
    public void indexImprove2(String dataDirectory,  IndexWriter fsWriter) throws Exception {



        //拿到对应文件夹的所有文件, 用递归处理所有目录
        File[] files = new File(dataDirectory).listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory() && !files[i].isHidden()) {
                indexImprove2(files[i].getCanonicalFile().toString(), fsWriter);
            } else if (files[i].getName().indexOf(".") != -1 && !files[i].isHidden()) {


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
                        fsWriter.addDocument(doc);
                    } else if (fileType.equals("docx")) {
                        XWPFWordExtractor xwpfWordExtractor = new XWPFWordExtractor(
                                new XWPFDocument(in));
                        doc.add(new TextField("fileName", files[i].getName(), Field.Store.YES));
                        doc.add(new TextField("contents", xwpfWordExtractor.getText(), Field.Store.YES));
                        doc.add(new StringField("fullPath", files[i].getCanonicalPath(), Field.Store.YES));
                        xwpfWordExtractor.close();
                        fsWriter.addDocument(doc);
                    } else if (fileType.equals("pdf")) {
                        PDFParser parser = new PDFParser(in);
                        parser.parse();
                        PDDocument pdDocument = parser.getPDDocument();
                        PDFTextStripper stripper = new PDFTextStripper();
                        doc.add(new TextField("fileName", files[i].getName(), Field.Store.YES));
                        doc.add(new TextField("contents", stripper.getText(pdDocument), Field.Store.YES));
                        doc.add(new StringField("fullPath", files[i].getCanonicalPath(), Field.Store.YES));
                        fsWriter.addDocument(doc);
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
                        fsWriter.addDocument(doc);
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
                        fsWriter.addDocument(doc);
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
                        fsWriter.addDocument(doc);
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
                        fsWriter.addDocument(doc);
                    }


                }
            }
        }



        //对新文档创建索引后，维护用于自动补全的字典树
        Searcher.results = Searcher.searchAllFile(); //用HashMap在内存中保存拼音和文件名的对应关系
        Searcher.trie = Searcher.getTrie(Searcher.results);
        Searcher.abbrTrie = Searcher.getAbbrTrie(Searcher.results);

    }














    //先把索引添加到内存索引库中，再把内存索引库中的索引批量写入到磁盘索引库中，减少磁盘I/O，这也是创建索引时的最大性能瓶颈。
    @SuppressWarnings("Duplicates")
    public void ramIndex(String dataDirectory, String indexDirectory) throws Exception {

        Directory directory = FSDirectory.open(Paths.get(indexDirectory));

        //拿到对应文件夹的所有文件, 用递归处理所有目录
        File[] files = new File(dataDirectory).listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory() && !files[i].isHidden()) {
                ramIndex(files[i].getCanonicalFile().toString(), indexDirectory);
            } else if (files[i].getName().indexOf(".") != -1 && !files[i].isHidden()) {


                //每次操作文件都获取一次，然后close()
                //根据索引库获得操作本地磁盘索引库的IndexWriter
                IndexWriter fsWriter = getFsWriter(directory);

                //根据索引库获得操作内存索引库的IndexWriter
                Directory ramDirectory = new RAMDirectory();
                IndexWriter ramWriter = getRamWriter(ramDirectory);


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
                        ramWriter.addDocument(doc);
                    } else if (fileType.equals("docx")) {
                        XWPFWordExtractor xwpfWordExtractor = new XWPFWordExtractor(
                                new XWPFDocument(in));
                        doc.add(new TextField("fileName", files[i].getName(), Field.Store.YES));
                        doc.add(new TextField("contents", xwpfWordExtractor.getText(), Field.Store.YES));
                        doc.add(new StringField("fullPath", files[i].getCanonicalPath(), Field.Store.YES));
                        xwpfWordExtractor.close();
                        ramWriter.addDocument(doc);
                    } else if (fileType.equals("pdf")) {
                        PDFParser parser = new PDFParser(in);
                        parser.parse();
                        PDDocument pdDocument = parser.getPDDocument();
                        PDFTextStripper stripper = new PDFTextStripper();
                        doc.add(new TextField("fileName", files[i].getName(), Field.Store.YES));
                        doc.add(new TextField("contents", stripper.getText(pdDocument), Field.Store.YES));
                        doc.add(new StringField("fullPath", files[i].getCanonicalPath(), Field.Store.YES));
                        ramWriter.addDocument(doc);
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
                        ramWriter.addDocument(doc);
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
                        ramWriter.addDocument(doc);
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
                        ramWriter.addDocument(doc);
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
                        ramWriter.addDocument(doc);
                    }

                    ramWriter.close();
                    //把内存索引库中的索引添加到磁盘索引库中
                    fsWriter.addIndexes(new Directory[]{ramDirectory});
                    fsWriter.close();
                }
            }
        }


        //对新文档创建索引后，维护用于自动补全的字典树
        Searcher.results = Searcher.searchAllFile(); //用HashMap在内存中保存拼音和文件名的对应关系
        Searcher.trie = Searcher.getTrie(Searcher.results);
        Searcher.abbrTrie = Searcher.getAbbrTrie(Searcher.results);

    }


    //先把索引添加到内存索引库中，再把内存索引库中的索引批量写入到磁盘索引库中，减少磁盘I/O，这也是创建索引时的最大性能瓶颈。
    @SuppressWarnings("Duplicates")
    public void ramIndexImprove(String dataDirectory, String indexDirectory , IndexWriter fsWriter, IndexWriter ramWriter) throws Exception {

        //拿到对应文件夹的所有文件, 用递归处理所有目录
        File[] files = new File(dataDirectory).listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory() && !files[i].isHidden()) {
                ramIndexImprove(files[i].getCanonicalFile().toString(), indexDirectory, fsWriter, ramWriter);
            } else if (files[i].getName().indexOf(".") != -1 && !files[i].isHidden()) {

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
                        ramWriter.addDocument(doc);
                    } else if (fileType.equals("docx")) {
                        XWPFWordExtractor xwpfWordExtractor = new XWPFWordExtractor(
                                new XWPFDocument(in));
                        doc.add(new TextField("fileName", files[i].getName(), Field.Store.YES));
                        doc.add(new TextField("contents", xwpfWordExtractor.getText(), Field.Store.YES));
                        doc.add(new StringField("fullPath", files[i].getCanonicalPath(), Field.Store.YES));
                        xwpfWordExtractor.close();
                        ramWriter.addDocument(doc);
                    } else if (fileType.equals("pdf")) {
                        PDFParser parser = new PDFParser(in);
                        parser.parse();
                        PDDocument pdDocument = parser.getPDDocument();
                        PDFTextStripper stripper = new PDFTextStripper();
                        doc.add(new TextField("fileName", files[i].getName(), Field.Store.YES));
                        doc.add(new TextField("contents", stripper.getText(pdDocument), Field.Store.YES));
                        doc.add(new StringField("fullPath", files[i].getCanonicalPath(), Field.Store.YES));
                        ramWriter.addDocument(doc);
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
                        ramWriter.addDocument(doc);
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
                        ramWriter.addDocument(doc);
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
                        ramWriter.addDocument(doc);
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
                        //倒排索引中不存储文档中的内容contents
                        doc.add(new TextField("contents", text, Field.Store.YES));
                        //分词器对文档的完整路径不进行分词
                        doc.add(new StringField("fullPath", files[i].getCanonicalPath(), Field.Store.YES));
                        extractor.close();
                        ramWriter.addDocument(doc);
                    }
                }
            }

        }


//        //对新文档创建索引后，维护用于自动补全的字典树
//        Searcher.results = Searcher.searchAllFile(); //用HashMap在内存中保存拼音和文件名的对应关系
//        Searcher.trie = Searcher.getTrie(Searcher.results);
//        Searcher.abbrTrie = Searcher.getAbbrTrie(Searcher.results);

    }



    //先把索引添加到内存索引库中，再把内存索引库中的索引批量写入到磁盘索引库中，减少磁盘I/O，这也是创建索引时的最大性能瓶颈。
    @SuppressWarnings("Duplicates")
    public void ramIndexImprove2(String dataDirectory, String indexDirectory) throws Exception {


        //拿到对应文件夹的所有文件, 用递归处理所有目录
        File[] files = new File(dataDirectory).listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory() && !files[i].isHidden()) {
                ramIndexImprove2(files[i].getCanonicalFile().toString(), indexDirectory);
            } else if (files[i].getName().indexOf(".") != -1 && !files[i].isHidden()) {
                Directory fsDirectory = FSDirectory.open(Paths.get(indexDirectory));
                Directory ramDirectory = new RAMDirectory();

                IndexWriter fsWriter = getFsWriter(fsDirectory);
                IndexWriter ramWriter = getRamWriter(ramDirectory);


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
                        ramWriter.addDocument(doc);
                    } else if (fileType.equals("docx")) {
                        XWPFWordExtractor xwpfWordExtractor = new XWPFWordExtractor(
                                new XWPFDocument(in));
                        doc.add(new TextField("fileName", files[i].getName(), Field.Store.YES));
                        doc.add(new TextField("contents", xwpfWordExtractor.getText(), Field.Store.YES));
                        doc.add(new StringField("fullPath", files[i].getCanonicalPath(), Field.Store.YES));
                        xwpfWordExtractor.close();
                        ramWriter.addDocument(doc);
                    } else if (fileType.equals("pdf")) {
                        PDFParser parser = new PDFParser(in);
                        parser.parse();
                        PDDocument pdDocument = parser.getPDDocument();
                        PDFTextStripper stripper = new PDFTextStripper();
                        doc.add(new TextField("fileName", files[i].getName(), Field.Store.YES));
                        doc.add(new TextField("contents", stripper.getText(pdDocument), Field.Store.YES));
                        doc.add(new StringField("fullPath", files[i].getCanonicalPath(), Field.Store.YES));
                        ramWriter.addDocument(doc);
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
                        ramWriter.addDocument(doc);
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
                        ramWriter.addDocument(doc);
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
                        ramWriter.addDocument(doc);
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
                        ramWriter.addDocument(doc);
                    }
                }
                ramWriter.close();
                fsWriter.addIndexes(new Directory[]{ramDirectory});
                fsWriter.close();
            }//处理一个文件结束




        }//for循环结束

//        ramWriter.close();
//        fsWriter.addIndexes(new Directory[]{ramDirectory});
//        fsWriter.close();


        //对新文档创建索引后，维护用于自动补全的字典树
        Searcher.results = Searcher.searchAllFile(); //用HashMap在内存中保存拼音和文件名的对应关系
        Searcher.trie = Searcher.getTrie(Searcher.results);
        Searcher.abbrTrie = Searcher.getAbbrTrie(Searcher.results);

    }



    @Test
    public void testRamIndex2FSIndex() throws Exception {
        String dataDirectory = "C:\\Users\\EDZ\\Desktop\\搜索引擎测试目录";
        String indexDirectory = "C:\\Users\\EDZ\\Desktop\\ramIndex_directory";

        //根据索引库获得操作内存索引库的IndexWriter

        Indexer indexer = new Indexer();
        long start = System.currentTimeMillis();
        indexer.ramIndex(dataDirectory, indexDirectory);
        long end = System.currentTimeMillis();
        System.out.println("基于内存索引库，一共对" + number + "个文件创建了索引，耗时" + (end - start) / 1000 + "秒");
    }


    @Test
    public void testFSIndex() throws Exception {

        String dataDirectory = "C:\\Users\\EDZ\\Desktop\\搜索引擎测试目录";
        String indexDirectory = "C:\\Users\\EDZ\\Desktop\\fsIndex_directory";

        Indexer indexer = new Indexer();
        long start = System.currentTimeMillis();
        indexer.index(dataDirectory, indexDirectory);
        long end = System.currentTimeMillis();
        System.out.println("基于磁盘索引库，一共对" + number + "个文件创建了索引，耗时" + (end - start) / 1000 + "秒");
    }




    @Test
    public void testFSIndexImprove() throws Exception {
        String dataDirectory = "C:\\Users\\EDZ\\Desktop\\搜索引擎测试目录";
        String indexDirectory = "C:\\Users\\EDZ\\Desktop\\fsIndex_directory";
        Directory directory = FSDirectory.open(Paths.get(indexDirectory));
        IndexWriter fsWriter = getFsWriter(directory);
        Indexer indexer = new Indexer();
        long start = System.currentTimeMillis();
        indexer.indexImprove(dataDirectory, indexDirectory, fsWriter);
        long end = System.currentTimeMillis();
        fsWriter.close();
        System.out.println("基于磁盘索引库，一共对" + number + "个文件创建了索引，耗时" + (end - start) / 1000 + "秒");
    }

    /**
     * 这个磁盘索引库效果最好
     * @throws Exception
     */
    @Test
    public void testFSIndexImprove2() throws Exception {
        String dataDirectory = "C:\\Users\\EDZ\\Desktop\\搜索引擎测试目录";
        String indexDirectory = "C:\\Users\\EDZ\\Desktop\\fsIndex_directory";
        Directory directory = FSDirectory.open(Paths.get(indexDirectory));
        IndexWriter fsWriter = getFsWriter(directory);
        Indexer indexer = new Indexer();
        long start = System.currentTimeMillis();
        indexer.indexImprove2(dataDirectory, fsWriter);
        long end = System.currentTimeMillis();
        fsWriter.close();
        System.out.println("基于磁盘索引库，一共对" + number + "个文件创建了索引，耗时" + (end - start)  + "毫秒");
    }


    /**
     * 这个内存数据库效果最好
     * @throws Exception
     */
    @Test
    public void testRamIndex2FSIndexImprove() throws Exception {
        String dataDirectory = "C:\\Users\\EDZ\\Desktop\\搜索引擎测试目录";
        String indexDirectory = "C:\\Users\\EDZ\\Desktop\\ramIndex_directory";

        Directory directory = FSDirectory.open(Paths.get(indexDirectory));

        //根据索引库获得操作内存索引库的IndexWriter
        Directory ramDirectory = new RAMDirectory();
        IndexWriter fsWriter = getFsWriter(directory);
        IndexWriter ramWriter = getRamWriter(ramDirectory);



        Indexer indexer = new Indexer();
        long start = System.currentTimeMillis();
        indexer.ramIndexImprove(dataDirectory, indexDirectory, fsWriter, ramWriter);
        ramWriter.forceMerge(5);//内存索引优化
        ramWriter.close();
        //把内存索引库中的索引添加到磁盘索引库中
        fsWriter.addIndexes(new Directory[]{ramDirectory});
        fsWriter.close();

        long end = System.currentTimeMillis();
        double second = (end-start)/1000.0;
        System.out.println("基于内存索引库，一共对" + number + "个文件创建了索引，耗时" + second + "秒");
    }


//    @Test
//    public void testRamIndex2FSIndexImprove2() throws Exception {
//        String dataDirectory = "C:\\Users\\EDZ\\Desktop\\搜索引擎测试目录";
//        String indexDirectory = "C:\\Users\\EDZ\\Desktop\\ramIndex_directory";
//
//
//        Indexer indexer = new Indexer();
//        long start = System.currentTimeMillis();
//        indexer.ramIndexImprove2(dataDirectory, indexDirectory);
//
//        long end = System.currentTimeMillis();
//        System.out.println("基于内存索引库，一共对" + number + "个文件创建了索引，耗时" + (end - start) / 1000 + "秒");
//    }





}
