package io.github.alaugks.spring.requesturilocaleinterceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import io.github.alaugks.spring.requesturilocaleinterceptor.mocks.MockLocaleResolver;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.DispatcherServlet;

class RequestURILocaleInterceptorBuilderTest {

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

    @Test
    void test_defaultRequestURI_notDefined() {
        this.mockRequest.setRequestURI("/it");
        RequestURILocaleInterceptor interceptor = RequestURILocaleInterceptor
            .builder()
            .defaultLocale(defaultLocal)
            .supportedLocales(supportedLocales)
            .build();
        interceptor.preHandle(this.mockRequest, this.mockedResponse, null);

        assertEquals("/en", this.mockedResponse.getRedirectedUrl());
    }

    @Test
    void test_defaultRequestURI_notDefined_defaultLocaleConstructor() {
        this.mockRequest.setRequestURI("/it");
        RequestURILocaleInterceptor interceptor = RequestURILocaleInterceptor
            .builder(defaultLocal)
            .supportedLocales(supportedLocales)
            .build();
        interceptor.preHandle(this.mockRequest, this.mockedResponse, null);

        assertEquals("/en", this.mockedResponse.getRedirectedUrl());
    }

    @Test
    void test_supportedLocales_notDefined() {
        this.mockRequest.setRequestURI("/it");
        RequestURILocaleInterceptor interceptor = RequestURILocaleInterceptor
            .builder()
            .defaultLocale(defaultLocal)
            .build();
        interceptor.preHandle(this.mockRequest, this.mockedResponse, null);

        assertEquals("/en", this.mockedResponse.getRedirectedUrl());
    }

    @Test
    void test_defaultLocale_notDefined() {
        try {
            this.mockRequest.setRequestURI("/it");
            RequestURILocaleInterceptor interceptor = RequestURILocaleInterceptor
                .builder()
                .build();
            interceptor.preHandle(this.mockRequest, this.mockedResponse, null);
            fail("Exception not thrown");
        } catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
            assertEquals("Default locale is null", e.getMessage());
        }
    }

    @Test
    void test_defaultLocale_isEmpty() {
        try {
            this.mockRequest.setRequestURI("/it");
            RequestURILocaleInterceptor interceptor = RequestURILocaleInterceptor
                .builder()
                .defaultLocale(Locale.forLanguageTag(""))
                .build();
            interceptor.preHandle(this.mockRequest, this.mockedResponse, null);
            fail("Exception not thrown");
        } catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
            assertEquals("Default locale is empty", e.getMessage());
        }
    }
}
