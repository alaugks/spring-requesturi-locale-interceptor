package io.github.alaugks.spring.requesturilocaleinterceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.alaugks.spring.requesturilocaleinterceptor.mocks.MockLocaleResolver;
import java.io.IOException;
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
            add(Locale.forLanguageTag("de"));
        }
    };
    static Locale defaultLocal = Locale.forLanguageTag("en");
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

    void initUrlLocaleInterceptor() throws IOException {
        RequestURILocaleInterceptor interceptor = RequestURILocaleInterceptor
            .builder()
            .defaultLocale(defaultLocal)
            .supportedLocales(supportedLocales)
            .defaultRequestURI("/en/home")
            .build();
        interceptor.preHandle(this.mockRequest, this.mockedResponse, null);
    }

    @Test
    void test_uriDePath() throws IOException {
        this.mockRequest.setRequestURI("/de/home");
        this.initUrlLocaleInterceptor();

        assertEquals("de", this.mockLocaleResolver.resolveLocale(this.mockRequest).getLanguage());
    }

    @Test
    void test_uriEnPath() throws IOException {
        this.mockRequest.setRequestURI("/en/home");
        this.initUrlLocaleInterceptor();

        assertEquals("en", this.mockLocaleResolver.resolveLocale(this.mockRequest).getLanguage());
    }

    @Test
    void test_redirectIfNotSupportedLocaleInUri_checkReturn() throws IOException {
        this.mockRequest.setRequestURI("/it/home");
        this.initUrlLocaleInterceptor();

        // In the case of a redirect, Request is not set for MockLocaleResolver. MockLocaleResolver.resolveLocale
        // throws a NullPointerException. This can be used to test if the response is set correctly and to abort
        // processing in RequestURILocaleInterceptor with a return false.
        assertThrows(NullPointerException.class, () -> this.mockLocaleResolver.resolveLocale(this.mockRequest));
    }

    @Test
    void test_redirectIfNotSupportedLocaleInUri() throws IOException {
        this.mockRequest.setRequestURI("/it/home");
        this.initUrlLocaleInterceptor();

        assertEquals("/en/home", this.mockedResponse.getRedirectedUrl());
    }

    @Test
    void test_redirectIfNotSupportedLocaleInUriWithOutPath() throws IOException {
        this.mockRequest.setRequestURI("/it");
        this.initUrlLocaleInterceptor();

        assertEquals("/en/home", this.mockedResponse.getRedirectedUrl());
    }

    @Test
    void test_fullUrl() throws IOException {
        this.mockRequest.setProtocol("https");
        this.mockRequest.setRemoteHost("www.example.com");
        this.mockRequest.setRemotePort(1234);
        this.mockRequest.setRequestURI("/it/home");
        this.mockRequest.setQueryString("param1=value1&param2=value2");
        this.initUrlLocaleInterceptor();

        assertEquals(
            "/en/homeparam1=value1&param2=value2",
            this.mockedResponse.getRedirectedUrl()
        );
    }
}
