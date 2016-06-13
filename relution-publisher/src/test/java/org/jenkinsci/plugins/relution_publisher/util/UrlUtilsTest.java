
package org.jenkinsci.plugins.relution_publisher.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;


public class UrlUtilsTest {

    @Test
    public void testCombineBase() {
        final String result = UrlUtils.combine("http://example.com", "a", "b");

        assertThat(result).as("Combined Url").isEqualTo("http://example.com/a/b");
    }

    @Test
    public void testCombineBaseWithSlash() {
        final String result = UrlUtils.combine("http://example.com", "/a", "b/");

        assertThat(result).as("Combined Url").isEqualTo("http://example.com/a/b");
    }

    @Test
    public void testCombineWrap() {
        final String result = UrlUtils.combine("http://example.com", "a", "http://test.com", "b");

        assertThat(result).as("Combined Url").isEqualTo("http://test.com/b");
    }

    @Test
    public void testCombineWrapWithSlash() {
        final String result = UrlUtils.combine("http://example.com", "/a", "http://test.com", "b/");

        assertThat(result).as("Combined Url").isEqualTo("http://test.com/b");
    }

    @Test
    public void testSanitizeUrl() {
        final String result = UrlUtils.sanitizePath("http://example.com");

        assertThat(result).as("Sanitized Path").isEqualTo("http://example.com");
    }

    @Test
    public void testSanitizeUrlWithSlash() {
        final String result = UrlUtils.sanitizePath("http://example.com/");

        assertThat(result).as("Sanitized Path").isEqualTo("http://example.com");
    }

    @Test
    public void testSanitizePath() {
        final String result = UrlUtils.sanitizePath("/a/b/c");

        assertThat(result).as("Sanitized Path").isEqualTo("/a/b/c");
    }

    @Test
    public void testSanitizePathWithSlash() {
        final String result = UrlUtils.sanitizePath("/a/b/c/");

        assertThat(result).as("Sanitized Path").isEqualTo("/a/b/c");
    }

    @Test
    public void shouldBeHttpUrl() {
        final boolean result = UrlUtils.isHttpUrl("http://example.com");

        assertThat(result).as("Is HTTP URL").isTrue();
    }

    @Test
    public void shouldNotBeHttpUrl() {
        final boolean result = UrlUtils.isHttpUrl("https://example.com");

        assertThat(result).as("Is HTTP URL").isFalse();
    }

    @Test
    public void shouldBeHttpsUrl() {
        final boolean result = UrlUtils.isHttpsUrl("https://example.com");

        assertThat(result).as("Is HTTPS URL").isTrue();
    }

    @Test
    public void shouldNotBeHttpsUrl() {
        final boolean result = UrlUtils.isHttpsUrl("http://example.com");

        assertThat(result).as("Is HTTPS URL").isFalse();
    }

    @Test
    public void testToBaseUrl() {
        final String result = UrlUtils.toBaseUrl("https://example.com:1234/a/b?c=d&e=f");

        assertThat(result).as("Base Url").isEqualTo("https://example.com");
    }

    @Test
    public void shouldReturnBaseUrl() {
        final String result = UrlUtils.toBaseUrl("https://example.com");

        assertThat(result).as("Base Url").isEqualTo("https://example.com");
    }

    @Test
    public void shouldReturnBaseUrlFromSlash() {
        final String result = UrlUtils.toBaseUrl("https://example.com/");

        assertThat(result).as("Base Url").isEqualTo("https://example.com");
    }

    @Test
    public void shouldReturnNullForNonUrl() {
        final String result = UrlUtils.toBaseUrl("some string");

        assertThat(result).as("Base Url").isNull();
    }
}
