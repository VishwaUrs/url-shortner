package com.urlshortener.repository;

import com.urlshortener.model.UrlListItem;
import jakarta.annotation.PostConstruct;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class ShortenedUrlRepository {

    private final JdbcTemplate jdbc;

    public ShortenedUrlRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostConstruct
    public void init() {
        jdbc.execute("CREATE TABLE IF NOT EXISTS SHORTENED_URLS ("
                + "ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "ALIAS TEXT NOT NULL UNIQUE, "
                + "FULL_URL TEXT NOT NULL, "
                + "CREATED_AT TEXT NOT NULL"
                + ")");
    }

    public boolean existsByAlias(String alias) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(1) FROM SHORTENED_URLS WHERE ALIAS = ?",
                Integer.class,
                alias);
        return count != null && count > 0;
    }

    public Optional<String> checkAndRetrieveAliasIfURLExists(String fullUrl){
        try {
            String alias = jdbc.queryForObject(
                    "SELECT ALIAS FROM SHORTENED_URLS WHERE FULL_URL = ?",
                    String.class,
                    fullUrl);
            return Optional.ofNullable(alias);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    public void save(String alias, String fullUrl, Instant createdAt) {
        jdbc.update("INSERT INTO SHORTENED_URLS (ALIAS, FULL_URL, CREATED_AT) VALUES (?, ?, ?)",
                alias, fullUrl, createdAt.toString());
    }

    public Optional<String> findFullUrlByAlias(String alias) {
        var results = jdbc.query("SELECT FULL_URL FROM SHORTENED_URLS WHERE ALIAS = ? LIMIT 1",
                (rs, rowNum) -> rs.getString("FULL_URL"),
                alias);
        return results.stream().findFirst();
    }

    public List<UrlListItem> findAll(String baseUrl) {
        return jdbc.query("SELECT ALIAS, FULL_URL, CREATED_AT FROM SHORTENED_URLS",
                urlListItemMapper(baseUrl));
    }

    public boolean deleteByAlias(String alias) {
        var rows = jdbc.update("DELETE FROM SHORTENED_URLS WHERE ALIAS = ?", alias);
        return rows > 0;
    }

    private RowMapper<UrlListItem> urlListItemMapper(String baseUrl) {
        return (ResultSet rs, int rowNum) -> new UrlListItem(
                rs.getString("ALIAS"),
                rs.getString("FULL_URL"),
                baseUrl + "/" + rs.getString("ALIAS"),
                rs.getString("CREATED_AT")
        );
    }
}
