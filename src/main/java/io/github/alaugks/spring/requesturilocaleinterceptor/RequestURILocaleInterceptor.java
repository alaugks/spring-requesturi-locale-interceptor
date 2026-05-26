// SPDX-License-Identifier: Apache-2.0
// Copyright 2024-2025 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.requesturilocaleinterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

/**
 * Spring MVC {@link HandlerInterceptor} that treats the first path segment of
 * the request URI as a locale (e.g. {@code /en/...}, {@code /de/...}).
 * <p>If the first segment matches one of the configured
 * {@link Builder#supportedLocales(List) supported locales}, the resolved
 * {@link Locale} is set on the request's {@link LocaleResolver} and the
 * request is allowed to proceed. Otherwise the request is redirected to the
 * same path prefixed with the default locale, falling back to
 * {@link Builder#defaultRequestURI(String) defaultRequestURI} when no path
 * remains.
 * <p>Use {@link #builder(Locale)} to obtain a {@link Builder} and call
 * {@link Builder#build()} to assemble the interceptor.
 */
public class RequestURILocaleInterceptor implements HandlerInterceptor {

    private static final String PATH_DELIMITER = "/";
    private final Locale defaultLocale;
    private final List<Locale> supportedLocales;
    private String defaultHomePath;

    /**
     * Creates a new interceptor from the values configured on the given
     * {@link Builder}.
     *
     * @param builder builder carrying the resolved configuration.
     */
    public RequestURILocaleInterceptor(Builder builder) {
        this.defaultLocale = builder.defaultLocale;
        this.supportedLocales = builder.supportedLocales;
        this.defaultHomePath = builder.defaultRequestURI;
    }

    /**
     * Creates a new {@link Builder} for assembling a
     * {@link RequestURILocaleInterceptor}.
     *
     * @param defaultLocale the locale used as fallback and as redirect prefix
     *                      when the request URI does not start with a
     *                      supported locale.
     * @return a new builder pre-configured with the given default locale.
     */
    public static Builder builder(Locale defaultLocale) {
        return new Builder(defaultLocale);
    }

    /**
     * Fluent builder for configuring and assembling a
     * {@link RequestURILocaleInterceptor}.
     */
    public static final class Builder {

        private final Locale defaultLocale;
        private List<Locale> supportedLocales;
        private String defaultRequestURI;

        /**
         * Creates a new builder with the given default locale.
         *
         * @param defaultLocale the locale used as fallback and as redirect
         *                      prefix when the request URI does not start
         *                      with a supported locale.
         */
        public Builder(Locale defaultLocale) {
            this.defaultLocale = defaultLocale;
        }

        /**
         * Lists all locales that are accepted as the first path segment of
         * the request URI. Locales not contained in this list trigger a
         * redirect to the {@link #defaultRequestURI(String) default
         * RequestURI}.
         *
         * @param supportedLocales the supported locales; if {@code null} an
         *                         empty list is assumed at {@link #build()}.
         * @return this builder for chaining.
         */
        public Builder supportedLocales(List<Locale> supportedLocales) {
            this.supportedLocales = supportedLocales;
            return this;
        }

        /**
         * Defines the path to redirect to when the request URI is empty or
         * does not start with a supported locale. The path may contain a
         * {@code %s} placeholder which is replaced with the default locale
         * tag (e.g. {@code /%s/home} resolves to {@code /en/home}). If not
         * set, the default RequestURI is {@code /{defaultLocale}}.
         *
         * @param defaultRequestURI redirect target path.
         * @return this builder for chaining.
         */
        public Builder defaultRequestURI(String defaultRequestURI) {
            this.defaultRequestURI = defaultRequestURI;
            return this;
        }

        /**
         * Assembles the configured {@link RequestURILocaleInterceptor}.
         *
         * @return the configured interceptor.
         * @throws IllegalArgumentException if the default locale is
         *                                  {@code null} or empty.
         */
        public RequestURILocaleInterceptor build() {
            Assert.notNull(defaultLocale, "Default locale is null");
            Assert.isTrue(!defaultLocale.toString().trim().isEmpty(), "Default locale is empty");

            if (this.supportedLocales == null) {
                this.supportedLocales = new ArrayList<>();
            }

            return new RequestURILocaleInterceptor(this);
        }
    }

    /**
     * Inspects the first path segment of the incoming request URI. If it
     * matches one of the configured supported locales, the locale is set on
     * the {@link LocaleResolver} and the request is allowed to proceed.
     * Otherwise the response is redirected to the same path prefixed with
     * the default locale (or to the configured default RequestURI when no
     * path remains).
     *
     * @param request  the current HTTP request.
     * @param response the current HTTP response.
     * @param handler  the chosen handler; unused.
     * @return {@code true} if the request should continue, {@code false} if
     *         a redirect was issued.
     * @throws RequestURILocaleInterceptorException if no
     *         {@link LocaleResolver} is available or any other error occurs
     *         while building the redirect URL.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
            if (localeResolver == null) {
                throw new IllegalStateException("LocaleResolver not found");
            }

            if (this.defaultHomePath == null) {
                this.defaultHomePath = PATH_DELIMITER + this.formatLocale(this.defaultLocale);
            }

            // Remove Leading Slash
            String requestUri = request.getRequestURI().substring(1);

            // Remove Trailing Slash
            String trailingSlash = "";
            if (requestUri.endsWith(PATH_DELIMITER)) {
                requestUri = requestUri.substring(0, requestUri.length() - 1);
                trailingSlash = PATH_DELIMITER;
            }

            String[] explodedRequestUri = requestUri.split(PATH_DELIMITER);

            Locale localeUri = Locale.forLanguageTag(explodedRequestUri[0]);

            boolean isLocaleSupported = this.supportedLocales
                .stream()
                .anyMatch(l -> l.toString().equals(localeUri.toString()));

            if (isLocaleSupported) {
                localeResolver.setLocale(request, response, localeUri);
                return true;
            }

            URL url = this.createUri(request, this.joinUri(explodedRequestUri, trailingSlash)).toURL();

            // Send redirect only with path + query.
            // No domain handling domain/ip vs. proxies and forwarded.
            response.sendRedirect(
                url.getPath() + (url.getQuery() != null ? "?" + url.getQuery() : "")
            );

            return false;

        } catch (Exception e) {
            throw new RequestURILocaleInterceptorException(e);
        }
    }

    private String joinUri(String[] uri, String trailingSlash) {
        String joinedUri = String.join(
            PATH_DELIMITER,
            Arrays.copyOfRange(uri, 1, uri.length)
        );

        String path = !joinedUri.isEmpty() ? PATH_DELIMITER + joinedUri : "";
        return !path.isEmpty()
            ? PATH_DELIMITER + this.formatLocale(this.defaultLocale) + path + trailingSlash
            : String.format(this.defaultHomePath, this.formatLocale(this.defaultLocale));
    }

    private String formatLocale(Locale locale) {
        return locale.toString().toLowerCase().replace("_", "-");
    }

    /**
     * Builds an absolute {@link URI} from the given request's scheme, host
     * and (non-default) port together with the supplied path and the
     * request's query string.
     *
     * @param req  the current HTTP request providing scheme, host, port and
     *             query string.
     * @param path the target path to use in the new URI.
     * @return the assembled absolute URI.
     */
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
