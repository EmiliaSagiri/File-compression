package com.example.filezip2;

import java.util.zip.ZipEntry;

public class Product {
    private String name;
    public Product(String name){
        this.name=name;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
