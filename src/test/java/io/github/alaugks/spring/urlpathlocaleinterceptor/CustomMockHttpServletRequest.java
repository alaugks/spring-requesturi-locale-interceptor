package io.github.alaugks.spring.urlpathlocaleinterceptor;

import java.util.Locale;
import org.springframework.mock.web.MockHttpServletRequest;

public class CustomMockHttpServletRequest extends MockHttpServletRequest {

    public Locale getLocale() {
        return Locale.forLanguageTag("");
    }
}
