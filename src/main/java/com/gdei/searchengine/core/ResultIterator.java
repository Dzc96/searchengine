package com.gdei.searchengine.core;

import com.gdei.searchengine.domain.Result;
import org.apache.lucene.search.suggest.InputIterator;
import org.apache.lucene.util.BytesRef;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @Description: 这个类是核心，决定了索引是如何创建的，决定了最终返回的提示关键词列表数据及其排序
 * 主要是对文件名建立联想
 */
public class ResultIterator implements InputIterator {
    private Iterator<Result> resultIterator;
    private Result currentResult;

    ResultIterator(Iterator<Result> resultIterator) {
        this.resultIterator = resultIterator;
    }

    /**
     * 设置是否启用Contexts域
     * @return
     */
    public boolean hasContexts() {
        return true;
    }

    /**
     * 是否有设置payload信息
     */
    public boolean hasPayloads() {
        return true;
    }

    public Comparator<BytesRef> getComparator() {
        return null;
    }

    /**
    * next方法的返回值指定的其实就是就是可能返回给我们的suggest的值的结果集合（LookUpResult.key),这里我们选择了商品名。
    */
    public BytesRef next() {
        if (resultIterator.hasNext()) {
            currentResult = resultIterator.next();
            try {
                //返回当前Project的name值，把product类的name属性值作为key
                return new BytesRef(currentResult.getFileName().getBytes("UTF8"));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("Couldn't convert to UTF-8",e);
            }
        } else {
            return null;
        }
    }

    /**
     * 将Result对象序列化存入payload
     * [这里仅仅是个示例，其实这种做法不可取,一般不会把整个对象存入payload,这样索引体积会很大，浪费硬盘空间]
     */
    public BytesRef payload() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(currentResult);
            out.close();
            return new BytesRef(bos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Well that's unfortunate.");
        }
    }

    /**
     * 把产品的销售区域存入context，context里可以是任意的自定义数据，一般用于数据过滤
     * Set集合里的每一个元素都会被创建一个TermQuery，你只是提供一个Set集合，至于new TermQuery
     * Lucene底层API去做了，但你必须要了解底层干了些什么
     */
    public Set<BytesRef> contexts() {
        try {
            Set<BytesRef> contents = new HashSet<BytesRef>();

            contents.add(new BytesRef(currentResult.getHighlighterFragment().getBytes("UTF8")));
            return contents;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Couldn't convert to UTF-8");
        }
    }

    /**
     * 返回权重值，这个值会影响排序
     * 这里以产品的销售量作为权重值，weight值即最终返回的热词列表里每个热词的权重值
     * 怎么设计返回这个权重值，发挥你们的想象力吧
     */
    public long weight() {
        return 1;
    }
}
