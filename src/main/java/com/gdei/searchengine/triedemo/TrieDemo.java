package com.gdei.searchengine.triedemo;

import com.gdei.searchengine.core.PinYin;
import com.gdei.searchengine.core.Searcher;
import com.gdei.searchengine.core.Trie;
import com.gdei.searchengine.domain.Result;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class TrieDemo {


    Searcher searcher = new Searcher();


    @Test
    public void test() {
        //根据文件名构建Trie字典树
        List<Result> results = searcher.searchAllFile();
        Trie trie = new Trie();
        Iterator<Result> iterator = results.iterator();
        while (iterator.hasNext()) {
            Result result = iterator.next();
            trie.insert(result.getFileName());
        }

        //根据前缀模糊查找Trie字典树
        List<String> datas = trie.getData("广东");
        System.out.println(datas);
    }



    @Test
    public void test1() {

        //根据文件名构建Trie字典树
        List<Result> results = searcher.searchAllFile();

        Trie trie = new Trie();
        Iterator<Result> iterator = results.iterator();
        while (iterator.hasNext()) {
            Result result = iterator.next();
            trie.insert(PinYin.getPinYin(result.getFileName()));
        }

        //preword传过来之后要作处理，有中文就过滤出来转成拼音

        //根据前缀模糊查找Trie字典树
        String preword = PinYin.getPinYin("广东第二shifan学院");
        System.out.println(preword);
        List<String> datas = trie.getData(preword);


        Iterator<String> iteratorData = datas.iterator();
        System.out.println("-------查出来的是英文---------");

        System.out.println(datas);

        System.out.println("---------------");

        Set<String> strings = Searcher.cn2pinyin.keySet();
//        System.out.println(strings);
        for (String key : strings) {
            String value = Searcher.cn2pinyin.get(key);
            System.out.println(key + " : " + value);
        }
        System.out.println("---------------");


        System.out.println("-------转换为中文---------");
        while (iteratorData.hasNext()) {
            String py = iteratorData.next();
            String cn = Searcher.cn2pinyin.get(py);
            System.out.println(cn);
        }
        

    }



    @Test
    public void test2() throws Exception {
        String key = "广东";
        List<Result> results = searcher.suggestSearchByTrie(key);

        Iterator<Result> iterator = results.iterator();
        while (iterator.hasNext()) {
            Result result = iterator.next();
            System.out.println(result.getFileName());
        }

    }

}
