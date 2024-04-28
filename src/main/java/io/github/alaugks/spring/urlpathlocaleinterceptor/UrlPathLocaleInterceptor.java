package io.github.alaugks.spring.urlpathlocaleinterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

public class UrlPathLocaleInterceptor implements HandlerInterceptor {

    private Locale defaultLocale;

    private List<Locale> supportedLocales;

    private String defaultHomePath;

    public void setDefaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    public void setSupportedLocales(List<Locale> supportedLocales) {
        this.supportedLocales = supportedLocales;
    }

    public void setDefaultHomePath(String defaultHomePath) {
        this.defaultHomePath = defaultHomePath;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws IOException {
        String[] uri = request.getRequestURI().trim().replaceAll("^/", "").split("/");
        String uriFromLocale = (0 < uri.length) ? uri[0] : null;

        if (uriFromLocale != null) {
            LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);

            if (localeResolver == null) {
                throw new IllegalStateException("LocaleResolver not found");
            }

            Locale localeUri = Locale.forLanguageTag(uriFromLocale);
            Locale locale;

            boolean supportedLocaleExists = this.supportedLocales
                .stream()
                .anyMatch(l -> l.toString().equals(localeUri.toString()));

            if (supportedLocaleExists) {
                locale = localeUri;
                localeResolver.setLocale(request, response, locale);
                return true;
            }

            String path = this.joinUriWithoutLang(uri);

            String queryString = "";
            if (request.getQueryString() != null) {
                queryString = !request.getQueryString().isEmpty() ? "?" + request.getQueryString() : "";
            }

            response.sendRedirect(!path.isEmpty()
                ? String.format("/%s%s", this.defaultLocale.toString(), path) + queryString
                : this.defaultHomePath + queryString);

            return false;

        }
        return true;
    }

    private String joinUriWithoutLang(String... uri) {
        String joinedUri = String.join(
            "/",
            Arrays.copyOfRange(uri, 1, uri.length)
        );
        return !joinedUri.isEmpty() ? "/" + joinedUri : "";
    }
}
