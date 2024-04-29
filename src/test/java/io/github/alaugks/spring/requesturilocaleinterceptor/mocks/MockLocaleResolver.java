package io.github.alaugks.spring.requesturilocaleinterceptor.mocks;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Locale;
import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.web.servlet.LocaleResolver;

public class MockLocaleResolver implements LocaleResolver {

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
