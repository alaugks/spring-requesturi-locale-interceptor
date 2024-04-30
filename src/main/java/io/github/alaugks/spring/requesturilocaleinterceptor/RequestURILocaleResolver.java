package io.github.alaugks.spring.requesturilocaleinterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Locale;
import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.web.servlet.i18n.AbstractLocaleResolver;

public class RequestURILocaleResolver extends AbstractLocaleResolver {

    static final String LOCALE_ATTRIBUTE = RequestURILocaleResolver.class.getName() + ".LOCALE";
    private HttpServletRequest request;

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        Object localAttribute = this.request.getAttribute(LOCALE_ATTRIBUTE);
        if (localAttribute != null) {
            return (Locale) localAttribute;
        }

        if (request.getLocale() != null) {
            return request.getLocale();
        }

        return this.getDefaultLocale();
    }

    @Override
    public void setLocale(HttpServletRequest request,
        HttpServletResponse response,
        Locale locale
    ) {
        request.setAttribute(LOCALE_ATTRIBUTE, new SimpleLocaleContext(locale).getLocale());
        this.request = request;
    }
}
