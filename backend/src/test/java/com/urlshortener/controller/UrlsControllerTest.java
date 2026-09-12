package com.urlshortener.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urlshortener.model.PagedResponse;
import com.urlshortener.model.ShortenUrlRequest;
import com.urlshortener.model.ShortenUrlResponse;
import com.urlshortener.model.UrlListItem;
import com.urlshortener.service.UrlShortenerService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UrlsControllerTest {

    private MockMvc mvc;
    @InjectMocks
    private UrlsController shortenURLsController;

    @Mock
    private HttpServletRequest requestContext;

    @Mock
    UrlShortenerService urlShortenerService;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(shortenURLsController).build();
    }

    @Test
    void test_ShorteningAFullUrlHappyPath() throws Exception {
        // Arrange
        var request = createUrlShorteningRequest("https://example.com/very/long/path", "ag2s");
        var expectedResponse = createShortenedUrlResponse("https://example.com/very/long/path", "https://snip.com/ag2s", "ag2s");
        when(requestContext.getScheme()).thenReturn("http");
        when(requestContext.getServerName()).thenReturn("localhost");
        when(requestContext.getServerPort()).thenReturn(8080);
        when(urlShortenerService.shorten(eq(request), eq("http://localhost:8080")))
                .thenReturn(expectedResponse);

        // Act
        ResponseEntity<ShortenUrlResponse> result = shortenURLsController.shorten(request, requestContext);

        // Assert
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(expectedResponse);
        assert result.getBody() != null;
        assertThat(result.getBody().getShortUrl().equals("https://snip.com/ag2s"));
        verify(urlShortenerService).shorten(request, "http://localhost:8080");
    }

    @Test
    void test_ShorteningAFullWhenFullUrlIsNotPresent_ShouldThrow400() throws Exception {
        var request = createUrlShorteningRequest("", "ag2s");
        ObjectMapper objectMapper = new ObjectMapper();
        mvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    private void stubRequestContext(String scheme, String serverName, int port) {
        when(requestContext.getScheme()).thenReturn(scheme);
        when(requestContext.getServerName()).thenReturn(serverName);
        when(requestContext.getServerPort()).thenReturn(port);
    }

    @Test
    void getAll_returnsSinglePage_whenTotalItemsFitWithinOnePage() {
        stubRequestContext("http", "localhost", 8080);
        List<UrlListItem> mockItems = List.of(
                new UrlListItem("abc", "https://example.com", "http://localhost:8080/abc", "2026-09-08T23:45:19Z"),
                new UrlListItem("def", "https://another.com", "http://localhost:8080/def", "2026-09-07T10:00:00Z")
        );
        when(urlShortenerService.findByPageNumber(0, 10, "http://localhost:8080")).thenReturn(mockItems);
        when(urlShortenerService.getNumberOfUrls()).thenReturn(2);

        PagedResponse<UrlListItem> result = shortenURLsController.getAll(requestContext, 0, 10);

        assertThat(result.getUrlList()).isEqualTo(mockItems);
        assertThat(result.getPage()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getTotalItems()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);
        verify(urlShortenerService).findByPageNumber(0, 10, "http://localhost:8080");
        verify(urlShortenerService).getNumberOfUrls();
    }


    @Test
    void getAll_returnsEmptyPage_whenNoUrlsExist() {
        stubRequestContext("http", "localhost", 8080);
        when(urlShortenerService.findByPageNumber(0, 10, "http://localhost:8080")).thenReturn(Collections.emptyList());
        when(urlShortenerService.getNumberOfUrls()).thenReturn(0);

        PagedResponse<UrlListItem> result = shortenURLsController.getAll(requestContext, 0, 10);

        assertThat(result.getUrlList()).isEmpty();
        assertThat(result.getTotalItems()).isEqualTo(0);
        assertThat(result.getTotalPages()).isEqualTo(0);
    }


    @Test
    void getAll_passesCustomPageAndSizeToService() {
        stubRequestContext("http", "localhost", 8080);
        when(urlShortenerService.findByPageNumber(2, 5, "http://localhost:8080")).thenReturn(List.of(new UrlListItem(),
                new UrlListItem(),new UrlListItem()
        ));
        when(urlShortenerService.getNumberOfUrls()).thenReturn(13);

        PagedResponse<UrlListItem> result = shortenURLsController.getAll(requestContext, 2, 5);

        assertThat(result.getUrlList().size()).isEqualTo(3);
        assertThat(result.getTotalItems()).isEqualTo(13);
        assertThat(result.getTotalPages()).isEqualTo(3);
        verify(urlShortenerService).findByPageNumber(2, 5, "http://localhost:8080");
    }

    @Test
    void getAll_returnsEmptyItems_whenRequestedPageIsBeyondLastPage() {
        stubRequestContext("http", "localhost", 8080);
        when(urlShortenerService.findByPageNumber(99, 10, "http://localhost:8080")).thenReturn(Collections.emptyList());
        when(urlShortenerService.getNumberOfUrls()).thenReturn(5);

        PagedResponse<UrlListItem> result = shortenURLsController.getAll(requestContext, 99, 10);

        assertThat(result.getUrlList()).isEmpty();
        assertThat(result.getPage()).isEqualTo(99);
        assertThat(result.getTotalItems()).isEqualTo(5);
        assertThat(result.getTotalPages()).isEqualTo(1);
    }


    @Test
    void redirectToUrl_returns302WithLocationHeader_whenAliasExists() {
        when(urlShortenerService.getFullUrl("abc")).thenReturn("https://example.com/very/long/path");

        ResponseEntity<Void> result = shortenURLsController.redirectToUrl("abc");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(result.getHeaders().getLocation()).isEqualTo(URI.create("https://example.com/very/long/path"));
        assertThat(result.getBody()).isNull();
        verify(urlShortenerService).getFullUrl("abc");
    }


    @Test
    void redirectToUrl_returns404_whenAliasDoesNotExist() {
        when(urlShortenerService.getFullUrl("missing")).thenReturn(null);

        ResponseEntity<Void> result = shortenURLsController.redirectToUrl("missing");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getHeaders().getLocation()).isNull();
        assertThat(result.getBody()).isNull();
        verify(urlShortenerService).getFullUrl("missing");
    }


    @Test
    void delete_returns204NoContent_whenAliasExistsAndIsDeleted() {
        when(urlShortenerService.delete("abc")).thenReturn(true);

        ResponseEntity<Void> result = shortenURLsController.delete("abc");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(result.getBody()).isNull();
        verify(urlShortenerService).delete("abc");
    }

    @Test
    void delete_returns404NotFound_whenAliasDoesNotExist() {
        when(urlShortenerService.delete("missing")).thenReturn(false);

        ResponseEntity<Void> result = shortenURLsController.delete("missing");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getBody()).isNull();
        verify(urlShortenerService).delete("missing");
    }


    public ShortenUrlResponse createShortenedUrlResponse(String aFullUrl, String aShortUrl, String aAlias){
        ShortenUrlResponse shortenUrlResponse = new ShortenUrlResponse();
        shortenUrlResponse.setFullUrl(aFullUrl);
        shortenUrlResponse.setShortUrl(aShortUrl);
        shortenUrlResponse.setAlias(aAlias);
        return shortenUrlResponse;
    }

    public ShortenUrlRequest createUrlShorteningRequest(String aFullUrl, String aAlias){
        ShortenUrlRequest shortenUrlRequest = new ShortenUrlRequest();
        shortenUrlRequest.setFullUrl(aFullUrl);
        shortenUrlRequest.setCustomAlias(aAlias);
        return shortenUrlRequest;
    }
}