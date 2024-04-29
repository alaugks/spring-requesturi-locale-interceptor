package io.github.alaugks.spring.requesturilocaleinterceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.alaugks.spring.requesturilocaleinterceptor.mocks.MockHttpServletRequest;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestURIResolverTest {

    private RequestURILocaleResolver localResolver;
    private org.springframework.mock.web.MockHttpServletRequest mockRequest;
    private MockHttpServletResponse mockResponse;

    @BeforeEach
    void beforeEach() {
        this.localResolver = new RequestURILocaleResolver();
        this.mockRequest = new org.springframework.mock.web.MockHttpServletRequest();
        this.mockResponse = new MockHttpServletResponse();
    }

    @Test
    void test_resolveLocale_defaultLocale() {
        org.springframework.mock.web.MockHttpServletRequest mockHttpServletRequest = mock(
            org.springframework.mock.web.MockHttpServletRequest.class);
        when(mockHttpServletRequest.getLocale()).thenReturn(null);
        this.localResolver.setDefaultLocale(Locale.forLanguageTag("de"));
        this.localResolver.setLocale(mockHttpServletRequest, this.mockResponse, null);

        assertEquals("de", this.localResolver.resolveLocale(mockHttpServletRequest).toString());
    }

    @Test
    void test_resolveLocale_withEmptyRequestLocale() {
        String userLanguage = System.getProperty("user.language");
        this.localResolver.setLocale(new MockHttpServletRequest(), this.mockResponse, null);

        assertEquals(userLanguage, this.localResolver.resolveLocale(this.mockRequest).toString());
    }

    @Test
    void test_resolveLocale_withLocale() {
        Locale locale = Locale.forLanguageTag("en-UK");
        this.localResolver.setLocale(new MockHttpServletRequest(), this.mockResponse, locale);

        assertEquals("en_UK", this.localResolver.resolveLocale(this.mockRequest).toString());
    }
}
