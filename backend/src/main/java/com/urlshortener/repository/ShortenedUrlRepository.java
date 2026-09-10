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

    /**
     * Creates the SHORTENED_URLS table in the database if it does not exist.
     */
    @PostConstruct
    public void init() {
        jdbc.execute("CREATE TABLE IF NOT EXISTS SHORTENED_URLS ("
                + "ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "ALIAS TEXT NOT NULL UNIQUE, "
                + "FULL_URL TEXT NOT NULL, "
                + "CREATED_AT TEXT NOT NULL"
                + ")");
    }

    /**
     * Check is there is an entry in the SHORTENED_URLS table for the alias.
     * @param alias
     * @return true if alias exists or false if it does not.
     */
    public boolean existsByAlias(String alias) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(1) FROM SHORTENED_URLS WHERE ALIAS = ?",
                Integer.class,
                alias);
        return count != null && count > 0;
    }

    /**
     * Check if the Full Url already has an alias created in the SHORTENED_URLS table and return the alias of the full url.
     * @param fullUrl
     * @return String alias if the Full URL is already shortened.
     */
    public Optional<String> checkAndRetrieveAliasIfURLExists(String fullUrl){
        try {
            String alias = jdbc.queryForObject(
                    "SELECT ALIAS FROM `SHORTENED_URLS` WHERE FULL_URL = ?",
                    String.class,
                    fullUrl);
            return Optional.ofNullable(alias);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * After the shortening of the URL, this method saves the alias, full url and created timestamp to the database table.
     * @param alias Alias of the Full URL
     * @param fullUrl Full URL
     * @param createdAt Timestamp as to when the URL was shortened
     */
    public void save(String alias, String fullUrl, Instant createdAt) {
        jdbc.update("INSERT INTO SHORTENED_URLS (ALIAS, FULL_URL, CREATED_AT) VALUES (?, ?, ?)",
                alias, fullUrl, createdAt.toString());
    }

    /**
     * Returns the Full URL if exists for the Alias passed
     * @param alias Alias of the Full URL
     * @return Full URL
     */
    public Optional<String> findFullUrlByAlias(String alias) {
        var results = jdbc.query("SELECT FULL_URL FROM SHORTENED_URLS WHERE ALIAS = ? LIMIT 1",
                (rs, rowNum) -> rs.getString("FULL_URL"),
                alias);
        return results.stream().findFirst();
    }

    /**
     * Returns all the rows from the database mapped to URLListItem
     * @param baseUrl Base URL used to generate the Shortened URL using the alias
     * @return List of UrlListItem
     */
    public List<UrlListItem> findAll(String baseUrl) {
        return jdbc.query("SELECT ALIAS, FULL_URL, CREATED_AT FROM SHORTENED_URLS",
                urlListItemMapper(baseUrl));
    }

    /**
     * Deletes the row containing the passed alias.
     * @param alias Alias of the Full URL
     * @return true if the
     */
    public boolean deleteByAlias(String alias) {
        var rows = jdbc.update("DELETE FROM SHORTENED_URLS WHERE ALIAS = ?", alias);
        return rows > 0;
    }

    /**
     * Logic to Map the row from SHORTENED_URLS table to UrlListItem
     * @param baseUrl Base Url to generate the shortened url
     * @return RowMapper to map the rows returned to UrlListItem
     */
    private RowMapper<UrlListItem> urlListItemMapper(String baseUrl) {
        return (ResultSet rs, int rowNum) -> new UrlListItem(
                rs.getString("ALIAS"),
                rs.getString("FULL_URL"),
                baseUrl + "/" + rs.getString("ALIAS"),
                rs.getString("CREATED_AT")
        );
    }

    /**
     * Returns the number of rows (or number of Shortened URls) from the database.
     * @return Count of Shortened URLs
     */
    public int countAll() {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM SHORTENED_URLS", Integer.class);
        return count != null ? count : 0;
    }

    /**
     * Use for pagination request. Returns the list of URLs based on the Page number and offset passed.
     * @param page Page number requested.
     * @param size Number of URLs / Rows that need to be returned
     * @param baseUrl Base URL for generating Shortened URL
     * @return List of UrlListItems
     */
    public List<UrlListItem> findPage(int page, int size, String baseUrl) {
        int offset = page * size;
        return jdbc.query(
                "SELECT ID, ALIAS, FULL_URL, CREATED_AT FROM SHORTENED_URLS " +
                        "ORDER BY CREATED_AT DESC LIMIT ? OFFSET ?",
                urlListItemMapper(baseUrl),
                size, offset);
    }
}
