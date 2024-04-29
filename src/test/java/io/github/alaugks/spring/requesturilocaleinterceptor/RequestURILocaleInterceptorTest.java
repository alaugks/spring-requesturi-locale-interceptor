package io.github.alaugks.spring.requesturilocaleinterceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.LocaleResolver;

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
        RequestURILocaleInterceptor interceptor = new RequestURILocaleInterceptor();
        interceptor.setDefaultLocale(defaultLocal);
        interceptor.setSupportedLocales(supportedLocales);
        interceptor.setDefaultHomePath("/en/home");
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

        assertEquals("http://localhost/en/home", this.mockedResponse.getRedirectedUrl());
    }

    @Test
    void test_redirectIfNotSupportedLocaleInUri_withQuery() throws IOException {

        this.mockRequest.setRequestURI("/it/home");
        this.mockRequest.setQueryString("param=value");
        this.initUrlLocaleInterceptor();

        assertEquals("http://localhost/en/home?param=value", this.mockedResponse.getRedirectedUrl());
    }

    @Test
    void test_redirectIfNotSupportedLocaleInUriWithOutPath() throws IOException {
        this.mockRequest.setRequestURI("/it");
        this.initUrlLocaleInterceptor();

        assertEquals("http://localhost/en/home", this.mockedResponse.getRedirectedUrl());
    }

    @Test
    void test_redirectIfNotSupportedLocaleInUriWithOutPath_withQuery() throws IOException {
        this.mockRequest.setRequestURI("/it");
        this.mockRequest.setQueryString("param=value");
        this.initUrlLocaleInterceptor();

        assertEquals("http://localhost/en/home?param=value", this.mockedResponse.getRedirectedUrl());
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
            "http://www.example.com:1234/en/home?param1=value1&param2=value2",
            this.mockedResponse.getRedirectedUrl()
        );
    }

    public static class MockLocaleResolver implements LocaleResolver {

        static final String MOCK_LOCALE_ATTRIBUTE = "LOCALE";
        HttpServletRequest request;
        HttpServletResponse response;

        @Override
        public Locale resolveLocale(HttpServletRequest request) {
            return (Locale) this.request.getAttribute(MOCK_LOCALE_ATTRIBUTE);
        }

        @Override
        public void setLocale(HttpServletRequest request,
            HttpServletResponse response,
            Locale locale
        ) {
            request.setAttribute(MOCK_LOCALE_ATTRIBUTE, new SimpleLocaleContext(locale).getLocale());
            this.request = request;
            this.response = response;
        }
    }
}
