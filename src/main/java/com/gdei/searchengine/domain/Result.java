package com.gdei.searchengine.domain;




public class Result {
    private String fileName;
    private String highlighterFragment;
    private String fullPath;

    public Result() {
    }

    public Result(String fileName, String highlighterFragment, String fullPath) {
        this.fileName = fileName;
        this.highlighterFragment = highlighterFragment;
        this.fullPath = fullPath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getHighlighterFragment() {
        return highlighterFragment;
    }

    public void setHighlighterFragment(String highlighterFragment) {
        this.highlighterFragment = highlighterFragment;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }
}
