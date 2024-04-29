package io.github.alaugks.spring.requesturilocaleinterceptor.mocks;

import java.util.Locale;

public class MockHttpServletRequest extends org.springframework.mock.web.MockHttpServletRequest {

    public Locale getLocale() {
        return Locale.forLanguageTag("");
    }
}
