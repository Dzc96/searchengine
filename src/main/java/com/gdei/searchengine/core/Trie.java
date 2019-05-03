package com.gdei.searchengine.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Trie {

    class Node {
        public Map<String, Node> nexts; // 子节点
        public int end;
        public Node() {
            this.nexts = new HashMap<String, Node>();
            this.end = 0;
        }
    }
    Node root = new Node();

    public void insert(String word) {
        if (word == null)
            return;
        Node node = root;
        for (int i = 0; i < word.length(); i++) {
            String str = "" + word.charAt(i);
            if (node.nexts.get(str) == null)
                node.nexts.put(str, new Node());
            node = node.nexts.get(str);
        }
        node.end = 1;
    }


    public boolean startWith(String preWord) {
        Node node = root;
        for (int i = 0; i < preWord.length(); i++) {
            String str = "" + preWord.charAt(i);
            if (node.nexts.get(str) == null)
                return false;
            node = node.nexts.get(str);
        }
        return true;
    }

    List<String> list = new ArrayList<String>();

    //根据这个方法，传入前缀，获取数据
    public List<String> getData(String preword) {
        list.clear();
        if (!startWith(preword))
            return null;
        else {
            StringBuilder str = new StringBuilder("");
            str.append(preword);
            Node node = root;
            for (int i = 0; i < preword.length(); i++)
                node = node.nexts.get("" + preword.charAt(i));
            dfs(node, str);
        }
        return list;
    }

    private void dfs(Node root, StringBuilder str) {
        if (root.end == 1) {
            list.add(str.toString());
            if (root.nexts.size() == 0)
                return;
        }
        Node node = root;
        for (String s : node.nexts.keySet()) {
            str.append(s);
            dfs(node.nexts.get(s), str);
            str.delete(str.length() - 1, str.length());
        }
    }


}
