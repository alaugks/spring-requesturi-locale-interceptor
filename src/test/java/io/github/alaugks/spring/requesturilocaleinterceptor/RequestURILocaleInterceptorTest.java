package io.github.alaugks.spring.requesturilocaleinterceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.alaugks.spring.requesturilocaleinterceptor.mocks.MockLocaleResolver;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.DispatcherServlet;

class RequestURILocaleInterceptorTest {

    static List<Locale> supportedLocales = new ArrayList<>() {
        {
            add(Locale.forLanguageTag("en"));
            add(Locale.forLanguageTag("en-US"));
            add(Locale.forLanguageTag("de"));
        }
    };
    MockHttpServletRequest mockRequest;
    MockHttpServletResponse mockedResponse;
    MockLocaleResolver mockLocaleResolver;

    @BeforeEach
    void beforeEach() {
        this.mockLocaleResolver = new MockLocaleResolver();
        this.mockRequest = new MockHttpServletRequest();
        this.mockRequest.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, this.mockLocaleResolver);
        this.mockedResponse = new MockHttpServletResponse();
    }

    void initUrlLocaleInterceptor(Locale defaultLocale, String defaultRequestUri) {
        RequestURILocaleInterceptor interceptor = RequestURILocaleInterceptor
            .builder(defaultLocale)
            .supportedLocales(supportedLocales)
            .defaultRequestURI(defaultRequestUri)
            .build();
        interceptor.preHandle(this.mockRequest, this.mockedResponse, null);
    }


    @ParameterizedTest
    @MethodSource("dataProvider_getLanguage")
    void test_uriLocale(String requestUri, String defaultLocale, String expected) {
        this.mockRequest.setRequestURI(requestUri);
        this.initUrlLocaleInterceptor(Locale.forLanguageTag(defaultLocale), "/en/home");

        assertEquals(expected, this.mockLocaleResolver.resolveLocale(this.mockRequest).getLanguage());
    }

    private static Stream<Arguments> dataProvider_getLanguage() {
        return Stream.of(
            // (String requestUri, String defaultLocale, String expected)
            Arguments.of("/en/home", "en", "en"),
            Arguments.of("/de/home", "de", "de"),
            Arguments.of("/en/home/", "en", "en")
        );
    }

    @ParameterizedTest
    @MethodSource("dataProvider_getRedirectedUrl")
    void test_redirect(String requestUri, String defaultLocale, String defaultRequestUri, String expected) {
        this.mockRequest.setRequestURI(requestUri);
        this.initUrlLocaleInterceptor(Locale.forLanguageTag(defaultLocale), defaultRequestUri);

        assertEquals(expected, this.mockedResponse.getRedirectedUrl());
    }

    private static Stream<Arguments> dataProvider_getRedirectedUrl() {
        return Stream.of(
            // (String requestUri, String defaultLocale, String defaultRequestUri, String expected)
            Arguments.of("/en/home", "en", "/en/home", null),
            Arguments.of("/it/home", "en", "/en/home", "/en/home"),
            Arguments.of("/it/home/", "en", "/%s/home", "/en/home/"),
            Arguments.of("/it/home/", "en", "/en/home", "/en/home/"),
            Arguments.of("/it/home", "en-US", "/%s/home", "/en-us/home"),
            Arguments.of("/it", "en", "/en/home", "/en/home"),
            Arguments.of("/it/", "en", "/en/home", "/en/home"),
            Arguments.of("/", "en", "/%s/home", "/en/home"),
            Arguments.of("/", "en", "/%s/home/", "/en/home/"),
            Arguments.of("/", "en", null, "/en")
        );
    }

    @Test
    void test_redirectIfNotSupportedLocaleInUri_checkReturn() {
        this.mockRequest.setRequestURI("/it/home");
        this.initUrlLocaleInterceptor(Locale.forLanguageTag("en"), "/en/home");

        // In the case of a redirect, Request is not set for MockLocaleResolver. MockLocaleResolver.resolveLocale
        // throws a NullPointerException. This can be used to test if the response is set correctly and to abort
        // processing in RequestURILocaleInterceptor with a return false.
        assertThrows(NullPointerException.class, () -> this.mockLocaleResolver.resolveLocale(this.mockRequest));
    }

    @Test
    void test_fullUrl() {
        URI uri = URI.create("https://www.example.com:1234/it/home?param1=value1&param2=value2");
        this.mockRequest.setProtocol(uri.getScheme());
        this.mockRequest.setRemoteHost(uri.getHost());
        this.mockRequest.setRemotePort(uri.getPort());
        this.mockRequest.setRequestURI(uri.getPath());
        this.mockRequest.setQueryString(uri.getQuery());
        this.initUrlLocaleInterceptor(Locale.forLanguageTag("en"), "/en/home");

        assertEquals(
            "/en/home?param1=value1&param2=value2",
            this.mockedResponse.getRedirectedUrl()
        );
    }

    @Test
    void test_throw_RequestURILocaleInterceptorException() {
        RequestURILocaleInterceptor interceptor = RequestURILocaleInterceptor
            .builder(Locale.forLanguageTag("en"))
            .build();

        assertThrows(
            RequestURILocaleInterceptorException.class,
            () -> interceptor.preHandle(this.mockRequest, this.mockedResponse, null)
        );
    }

    @Test
    void test_throw_getLocaleResolver_Exception() {
        this.mockRequest.setRequestURI("/");
        this.mockRequest.removeAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE);
        RequestURILocaleInterceptor interceptor = RequestURILocaleInterceptor
            .builder(Locale.forLanguageTag("en"))
            .build();

        var e = assertThrows(
            RequestURILocaleInterceptorException.class,
            () -> interceptor.preHandle(this.mockRequest, this.mockedResponse, null)
        );
        assertInstanceOf(IllegalStateException.class, e.getCause());
    }
}
