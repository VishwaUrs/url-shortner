package com.urlshortener.controller;

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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
    public List<UrlListItem> getAll(HttpServletRequest requestContext) {
        logger.debug("Inside GET list of Shorten Urls");
        var baseUrl = getBaseUrl(requestContext);
        return service.getAll(baseUrl);
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
