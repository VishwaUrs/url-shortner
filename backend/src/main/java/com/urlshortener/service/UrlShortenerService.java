package com.urlshortener.service;

import com.urlshortener.model.ShortenUrlRequest;
import com.urlshortener.model.ShortenUrlResponse;
import com.urlshortener.model.UrlListItem;
import com.urlshortener.repository.ShortenedUrlRepository;
import com.urlshortener.util.AliasGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class UrlShortenerService {

    private static final String ALIAS_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int GENERATED_ALIAS_LENGTH = 7;
    private final Logger logger = LogManager.getLogger(UrlShortenerService.class);
    private final ShortenedUrlRepository repository;

    public UrlShortenerService(ShortenedUrlRepository repository) {
        this.repository = repository;
    }

    public ShortenUrlResponse shorten(ShortenUrlRequest request, String baseUrl) {

        var fullUrl = normalizeUrl(request.getFullUrl());

        Optional<String> alias = repository.checkAndRetrieveAliasIfURLExists(fullUrl);

        if (alias.isEmpty()) {
            logger.debug("Full URL does not have an alias already present.");
            alias = Optional.of(request.getCustomAlias() == null || request.getCustomAlias().isBlank()
                    ? generateAlias()
                    : request.getCustomAlias().trim());

            if (!isValidAlias(alias.get())) {
                throw new IllegalArgumentException("Alias can only contain letters, numbers, and hyphens (2–64 characters).");
            }

            if (repository.existsByAlias(alias.get())) {
                //very rare case.
                throw new IllegalStateException("The newly generated Alias '" + alias + "' is already taken.");
            }

            repository.save(alias.get(), fullUrl, Instant.now());
        }
        return new ShortenUrlResponse(baseUrl + "/" + alias.get(), alias.get(), fullUrl);
    }

    public String getFullUrl(String alias) {
        return repository.findFullUrlByAlias(alias).orElse(null);
    }

    public List<UrlListItem> getAll(String baseUrl) {
        return repository.findAll(baseUrl);
    }

    public boolean delete(String alias) {
        var fullUrl = repository.findFullUrlByAlias(alias).orElse(null);
        if (fullUrl == null) {
            return false;
        }
        return repository.deleteByAlias(alias);
    }

    private static String generateAlias() {
        return AliasGenerator.generateRandomAlias(GENERATED_ALIAS_LENGTH);
    }

    private static boolean isValidAlias(String alias) {
        if (alias == null || alias.length() < 2 || alias.length() > 64) {
            return false;
        }
        return alias.chars().allMatch(c -> Character.isLetterOrDigit(c) || c == '-');
    }

    private static String normalizeUrl(String fullUrl) {
        if (fullUrl == null || fullUrl.isBlank()) {
            throw new IllegalArgumentException("Full Url is required.");
        }

        try {
            var uri = new URI(fullUrl.trim());
            if (uri.getScheme() == null || uri.getHost() == null) {
                throw new IllegalArgumentException("Full Url must be a valid URL. Host/Scheme is missing");
            }

            var scheme = uri.getScheme().toLowerCase();
            if (!scheme.equals("http") && !scheme.equals("https")) {
                throw new IllegalArgumentException("Full Url must be a valid URL. Unsupported Scheme");
            }

            var path = uri.getPath();
            if (path == null || path.isBlank()) {
                path = "/";
            }

            return new URI(
                    uri.getScheme(),
                    uri.getUserInfo(),
                    uri.getHost(),
                    uri.getPort(),
                    path,
                    uri.getQuery(),
                    uri.getFragment()
            ).toString();
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException("`Full Url must be a valid URL.");
        }
    }

    public List<UrlListItem> findByPageNumber(int page, int size, String baseUrl) {
        return repository.findPage(page, size, baseUrl);
    }

    public int getNumberOfUrls() {
        return repository.countAll();
    }
}
