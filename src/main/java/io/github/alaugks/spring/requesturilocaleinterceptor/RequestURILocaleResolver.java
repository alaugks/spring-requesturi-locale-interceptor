// SPDX-License-Identifier: Apache-2.0
// Copyright 2024-2025 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.requesturilocaleinterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Locale;
import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.web.servlet.i18n.AbstractLocaleResolver;

/**
 * {@link org.springframework.web.servlet.LocaleResolver LocaleResolver}
 * implementation that stores the locale chosen by
 * {@link RequestURILocaleInterceptor} as a request attribute.
 * <p>{@link #resolveLocale(HttpServletRequest)} returns, in order, the locale
 * previously set via {@link #setLocale(HttpServletRequest, HttpServletResponse, Locale)},
 * the request's {@link HttpServletRequest#getLocale() accept-language locale},
 * or the configured {@link #getDefaultLocale() default locale}.
 */
public class RequestURILocaleResolver extends AbstractLocaleResolver {

    static final String LOCALE_ATTRIBUTE = RequestURILocaleResolver.class.getName() + ".LOCALE";
    private HttpServletRequest request;

    /**
     * Resolves the locale for the current request.
     *
     * @param request the current HTTP request.
     * @return the locale previously set via
     *         {@link #setLocale(HttpServletRequest, HttpServletResponse, Locale)},
     *         the request's {@code Accept-Language} locale, or the
     *         configured default locale.
     */
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

    /**
     * Stores the given locale as a request attribute so subsequent calls to
     * {@link #resolveLocale(HttpServletRequest)} return it.
     *
     * @param request  the current HTTP request.
     * @param response the current HTTP response; unused.
     * @param locale   the locale to remember for this request.
     */
    @Override
    public void setLocale(HttpServletRequest request,
        HttpServletResponse response,
        Locale locale
    ) {
        request.setAttribute(LOCALE_ATTRIBUTE, new SimpleLocaleContext(locale).getLocale());
        this.request = request;
    }
}
