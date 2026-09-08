package com.urlshortener.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class AliasGeneratorTest {

    @Test
    void shorten_WithValidUrl_ReturnsShortUrlResponse()  {
        assertNotNull(AliasGenerator.randomAliasGenerate(7));
    }
}