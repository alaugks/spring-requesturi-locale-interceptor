package io.github.alaugks.spring.requesturilocaleinterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.springframework.util.Assert;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.util.UriComponentsBuilder;

public class RequestURILocaleInterceptor implements HandlerInterceptor {

    private final Locale defaultLocale;
    private final List<Locale> supportedLocales;
    private final String defaultHomePath;

    public RequestURILocaleInterceptor(Builder builder) {
        this.defaultLocale = builder.defaultLocale;
        this.supportedLocales =builder.supportedLocales;
        this.defaultHomePath = builder.defaultRequestURI;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private Locale defaultLocale;
        private List<Locale> supportedLocales;
        private String defaultRequestURI;

        public Builder defaultLocale(Locale defaultLocale) {
            this.defaultLocale = defaultLocale;
            return this;
        }

        public Builder supportedLocales(List<Locale> supportedLocales) {
            this.supportedLocales = supportedLocales;
            return this;
        }

        public Builder defaultRequestURI(String defaultRequestURI) {
            this.defaultRequestURI = defaultRequestURI;
            return this;
        }

        public RequestURILocaleInterceptor build() {
            Assert.notNull(defaultLocale, "Default locale is null");
            Assert.isTrue(!defaultLocale.toString().trim().isEmpty(), "Default locale is empty");

            if (this.supportedLocales == null) {
                this.supportedLocales = new ArrayList<>();
            }

            if (this.defaultRequestURI == null) {
                this.defaultRequestURI = String.format("/%s", this.defaultLocale);
            }

            return new RequestURILocaleInterceptor(this);
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        try {

            String[] uri = request.getRequestURI().substring(1).split("/");
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

                path = !path.isEmpty()
                    ? String.format("/%s%s", this.defaultLocale.toString(), path)
                    : this.defaultHomePath;

                URL url = this.createUri(request, path).toURL();

                String requestURI = String.format(
                    "%s%s",
                    url.getPath() != null ? url.getPath() : "",
                    url.getQuery() != null  ? url.getQuery() : ""
                );

                response.sendRedirect(requestURI);

                return false;

            }
        } catch (IOException e) {
            throw new RequestURILocaleInterceptorException(e);
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

    public URI createUri(final HttpServletRequest req, String path) {

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
            .newInstance()
            .scheme(req.getScheme())
            .host(req.getRemoteHost())
            .path(path)
            .query(req.getQueryString());

        if (!List.of(80, 443).contains(req.getRemotePort())) {
            uriBuilder.port(req.getRemotePort());
        }

        return uriBuilder.build().toUri();
    }
}
