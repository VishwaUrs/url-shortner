package com.urlshortener.model;

/*
Model to store and send the Full Url, its alias, shortened version and the created at timestamp.
 */
public class UrlListItem {
    private String alias;
    private String fullUrl;
    private String shortUrl;
    private String createdAt;

    public UrlListItem() {
    }

    public UrlListItem(String alias, String fullUrl, String shortUrl, String createdAt) {
        this.alias = alias;
        this.fullUrl = fullUrl;
        this.shortUrl = shortUrl;
        this.createdAt = createdAt;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getFullUrl() {
        return fullUrl;
    }

    public void setFullUrl(String fullUrl) {
        this.fullUrl = fullUrl;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }

    public String getCreatedAt() {return createdAt;}

    public void setCreatedAt(String createdAt) {this.createdAt = createdAt;}
}
