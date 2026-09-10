package com.urlshortener.model;

import jakarta.validation.constraints.NotBlank;

/**
 * Request Object structure
 */
public class ShortenUrlRequest {

    @NotBlank(message = "FullUrl is Required.")
    private String fullUrl;

    private String customAlias;

    public String getFullUrl() {
        return fullUrl;
    }

    public void setFullUrl(String fullUrl) {
        this.fullUrl = fullUrl;
    }

    public String getCustomAlias() {
        return customAlias;
    }

    public void setCustomAlias(String customAlias) {
        this.customAlias = customAlias;
    }
}
