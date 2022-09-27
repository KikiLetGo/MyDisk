package com.elexlab.mydisk.model;

import java.util.List;

public class FileDir {
    private String name;
    private List<String> documents;
    private List<FileDir> dirs;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getDocuments() {
        return documents;
    }

    public void setDocuments(List<String> documents) {
        this.documents = documents;
    }

    public List<FileDir> getDirs() {
        return dirs;
    }

    public void setDirs(List<FileDir> dirs) {
        this.dirs = dirs;
    }
}
