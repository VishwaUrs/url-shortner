package com.urlshortener.controller;

import com.urlshortener.model.PagedResponse;
import com.urlshortener.model.ShortenUrlRequest;
import com.urlshortener.model.ShortenUrlResponse;
import com.urlshortener.model.UrlListItem;
import com.urlshortener.service.UrlShortenerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@org.springframework.web.bind.annotation.CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class UrlsController {

    private final UrlShortenerService service;

    private final Logger logger = LogManager.getLogger(UrlsController.class);
    public UrlsController(UrlShortenerService service) {
        this.service = service;
    }

    @PostMapping("/shorten")
    public ResponseEntity<ShortenUrlResponse> shorten(
            @Valid @RequestBody ShortenUrlRequest request,
            HttpServletRequest requestContext) {
        logger.debug("Inside Post /Shorten method");
        var baseUrl = getBaseUrl(requestContext);
        ShortenUrlResponse shortenUrlResponse = service.shorten(request,baseUrl);
        return  ResponseEntity.ok().body(shortenUrlResponse);
    }

    @GetMapping("/urls")
    public PagedResponse<UrlListItem> getAll(HttpServletRequest requestContext,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        logger.debug("Inside GET list of Shorten Urls");
        var baseUrl = getBaseUrl(requestContext);
        List<UrlListItem> urls = service.findByPageNumber(page, size, baseUrl);
        int totalItems = service.getNumberOfUrls();
        int totalPages = (int) Math.ceil((double) totalItems / size);

        return new PagedResponse<>(urls, page, size, totalItems, totalPages);
    }

    @GetMapping("/{alias}")
    public ResponseEntity<Void> redirectToUrl(@PathVariable String alias) {
        logger.debug("Inside GET shortened url");
        var fullUrl = service.getFullUrl(alias);
        if (fullUrl == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(fullUrl)).build();
    }

    @DeleteMapping("/{alias}")
    public ResponseEntity<Void> delete(@PathVariable String alias) {
        logger.debug("Inside Delete Shorten Url");
        var deleted = service.delete(alias);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    private String getBaseUrl(HttpServletRequest request) {
        var scheme = request.getScheme();
        System.out.println("Scheme: "+scheme);
        var serverName = request.getServerName();
        System.out.println("serverName: "+serverName);
        var serverPort = request.getServerPort();
        System.out.println("serverPort: "+serverPort);
        var baseUrl = new StringBuilder(scheme).append("://").append(serverName);
        if (serverPort != 80 && serverPort != 443) {
            baseUrl.append(":").append(serverPort);
        }
        System.out.println("baseUrl: "+baseUrl);
        return baseUrl.toString();
    }
}
