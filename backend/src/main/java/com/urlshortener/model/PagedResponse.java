package com.urlshortener.model;

import java.util.List;

public class PagedResponse <T>{

    private List<T> urlList;
    private int page;
    private int size;
    private int totalItems;
    private int totalPages;

    public PagedResponse (List<T> urlList, int page, int size, int totalItems, int totalPaages) {
        this.urlList = urlList;
        this.page = page;
        this.size = size;
        this.totalItems = totalItems;
        this.totalPages = totalPaages;
    }
    public List<T> getUrlList() {
        return urlList;
    }

    public void setUrlList(List<T> urlList) {
        this.urlList = urlList;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
