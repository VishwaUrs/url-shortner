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

    /**
     * Shortens a long URL and returns the shortened URL (also called as Alias)
     * @param request ShortenUrlRequest object passed by the client
     * @param requestContext HttpServletRequest
     * @return ResponseEntity<ShortenUrlResponse>
     */
    @PostMapping("/shorten")
    public ResponseEntity<ShortenUrlResponse> shorten(
            @Valid @RequestBody ShortenUrlRequest request,
            HttpServletRequest requestContext) {
        logger.debug("Shortening the long URL {}", request.getFullUrl());
        var baseUrl = getBaseUrl(requestContext);
        ShortenUrlResponse shortenUrlResponse = service.shorten(request,baseUrl);
        logger.debug("Returning Shortened URL for the long URL {}", request.getFullUrl());
        return  ResponseEntity.ok().body(shortenUrlResponse);
    }

    /**
     * Fetches Shortened URLs from the database and returns it. If more than 10 Shortened URLs are present in the database,
     * first 10 would be returned and the subsequent ones will be returned as requested.
     * @param requestContext HttpServletRequest
     * @param page Page number to be retrieved
     * @param size Maximum number of URLs returned at a time. Default is 10.
     * @return List of URLs wrapped in PagedResponse Object.
     */
    @GetMapping("/urls")
    public PagedResponse<UrlListItem> getAll(HttpServletRequest requestContext,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        logger.debug("Getting list of Shortened URLs");
        var baseUrl = getBaseUrl(requestContext);
        List<UrlListItem> urls = service.findByPageNumber(page, size, baseUrl);
        int totalItems = service.getNumberOfUrls();
        int totalPages = (int) Math.ceil((double) totalItems / size);
        return new PagedResponse<>(urls, page, size, totalItems, totalPages);
    }

    /**
     * Gets the Full URL for the alias sent in.
     * @param alias String
     * @return ResponseEntity with status 302 and a redirection to the original URL.
     */
    @GetMapping("/{alias}")
    public ResponseEntity<Void> redirectToUrl(@PathVariable String alias) {
        logger.debug("Getting Shortened URL for alias : {}",alias);
        var fullUrl = service.getFullUrl(alias);
        if (fullUrl == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        logger.debug("Returning Full URL {} for alias : {}",fullUrl, alias);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(fullUrl)).build();
    }

    /**
     * Deletes the row from the table which has a Full URL corresponding to the alias passed.
     * @param alias String
     * @return Status 204 if deletion is successful or 404 if the requested alias is not present in the database table.
     */
    @DeleteMapping("/{alias}")
    public ResponseEntity<Void> delete(@PathVariable String alias) {
        logger.debug("Deleting Shortened URL with alias : {}", alias);
        var deleted = service.delete(alias);
        if(deleted) {
            logger.debug("URL Deleted : {}", alias);
        }
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    private String getBaseUrl(HttpServletRequest request) {
        var scheme = request.getScheme();
        var serverName = request.getServerName();
        var serverPort = request.getServerPort();
        var baseUrl = new StringBuilder(scheme).append("://").append(serverName);
        if (serverPort != 80 && serverPort != 443) {
            baseUrl.append(":").append(serverPort);
        }
        return baseUrl.toString();
    }
}
