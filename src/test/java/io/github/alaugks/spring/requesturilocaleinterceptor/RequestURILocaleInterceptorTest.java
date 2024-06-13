package io.github.alaugks.spring.requesturilocaleinterceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.alaugks.spring.requesturilocaleinterceptor.mocks.MockLocaleResolver;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

    void initUrlLocaleInterceptor(Locale defaultLocale) {
        RequestURILocaleInterceptor interceptor = RequestURILocaleInterceptor
            .builder(defaultLocale)
            .supportedLocales(supportedLocales)
            .defaultRequestURI("/en/home")
            .build();
        interceptor.preHandle(this.mockRequest, this.mockedResponse, null);
    }

    @Test
    void test_uriDePath() {
        this.mockRequest.setRequestURI("/de/home");
        this.initUrlLocaleInterceptor(Locale.forLanguageTag("en"));

        assertEquals("de", this.mockLocaleResolver.resolveLocale(this.mockRequest).getLanguage());
    }

    @Test
    void test_uriEnPath() {
        this.mockRequest.setRequestURI("/en/home");
        this.initUrlLocaleInterceptor(Locale.forLanguageTag("en"));

        assertEquals("en", this.mockLocaleResolver.resolveLocale(this.mockRequest).getLanguage());
    }

    @Test
    void test_redirectIfNotSupportedLocaleInUri_checkReturn() {
        this.mockRequest.setRequestURI("/it/home");
        this.initUrlLocaleInterceptor(Locale.forLanguageTag("en"));

        // In the case of a redirect, Request is not set for MockLocaleResolver. MockLocaleResolver.resolveLocale
        // throws a NullPointerException. This can be used to test if the response is set correctly and to abort
        // processing in RequestURILocaleInterceptor with a return false.
        assertThrows(NullPointerException.class, () -> this.mockLocaleResolver.resolveLocale(this.mockRequest));
    }

    @Test
    void test_redirectIfNotSupportedLocaleDefaultLocale() {
        this.mockRequest.setRequestURI("/it/home");
        this.initUrlLocaleInterceptor(Locale.forLanguageTag("en"));

        assertEquals("/en/home", this.mockedResponse.getRedirectedUrl());
    }

    @Test
    void test_redirectIfNotSupportedLocaleDefaultLocaleAndRegion() {
        this.mockRequest.setRequestURI("/it/home");
        this.initUrlLocaleInterceptor(Locale.forLanguageTag("en-us"));

        assertEquals("/en-us/home", this.mockedResponse.getRedirectedUrl());
    }

    @Test
    void test_redirectIfNotSupportedLocaleInUriWithOutPath() {
        this.mockRequest.setRequestURI("/it");
        this.initUrlLocaleInterceptor(Locale.forLanguageTag("en"));

        assertEquals("/en/home", this.mockedResponse.getRedirectedUrl());
    }

    @Test
    void test_fullUrl() {
        this.mockRequest.setProtocol("https");
        this.mockRequest.setRemoteHost("www.example.com");
        this.mockRequest.setRemotePort(1234);
        this.mockRequest.setRequestURI("/it/home");
        this.mockRequest.setQueryString("param1=value1&param2=value2");
        this.initUrlLocaleInterceptor(Locale.forLanguageTag("en"));

        assertEquals(
            "/en/homeparam1=value1&param2=value2",
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
