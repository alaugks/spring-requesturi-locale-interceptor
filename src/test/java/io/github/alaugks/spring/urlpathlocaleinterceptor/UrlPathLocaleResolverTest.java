package io.github.alaugks.spring.urlpathlocaleinterceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class UrlPathLocaleResolverTest {

    private UrlPathLocaleResolver localResolver;
    private MockHttpServletRequest mockRequest;
    private MockHttpServletResponse mockResponse;
    private CustomMockHttpServletRequest customMockRequest;

    @BeforeEach
    void beforeEach() {
        this.localResolver = new UrlPathLocaleResolver();
        this.mockRequest = new MockHttpServletRequest();
        this.customMockRequest = new CustomMockHttpServletRequest();
        this.customMockRequest.getLocale();
        this.mockResponse = new MockHttpServletResponse();
    }

    @Test
    void test_resolveLocale_withoutLocaleOnNull() {
        this.mockRequest.addPreferredLocale(Locale.forLanguageTag(""));
        this.localResolver.setLocale(this.mockRequest, this.mockResponse, null);
        assertEquals("", this.localResolver.resolveLocale(this.mockRequest).toString());
    }

    @Test
    void test_resolveLocale_withEmptyRequestLocale() {
        String userLanguage = System.getProperty("user.language");
        this.localResolver.setLocale(this.customMockRequest, this.mockResponse, null);
        assertEquals(userLanguage, this.localResolver.resolveLocale(this.mockRequest).toString());
    }

    @Test
    void test_resolveLocale_withLocale() {
        Locale locale = Locale.forLanguageTag("en-UK");
        this.localResolver.setLocale(this.customMockRequest, this.mockResponse, locale);
        assertEquals("en_UK", this.localResolver.resolveLocale(this.mockRequest).toString());
    }
}
