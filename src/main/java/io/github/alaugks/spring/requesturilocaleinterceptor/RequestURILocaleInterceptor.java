/*
 * Copyright 2024-2025 André Laugks <alaugks@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

public class RequestURILocaleInterceptor implements HandlerInterceptor {

    private static final String PATH_DELIMITER = "/";
    private final Locale defaultLocale;
    private final List<Locale> supportedLocales;
    private String defaultHomePath;

    public RequestURILocaleInterceptor(Builder builder) {
        this.defaultLocale = builder.defaultLocale;
        this.supportedLocales = builder.supportedLocales;
        this.defaultHomePath = builder.defaultRequestURI;
    }

    public static Builder builder(Locale defaultLocale) {
        return new Builder(defaultLocale);
    }

    public static final class Builder {

        private final Locale defaultLocale;
        private List<Locale> supportedLocales;
        private String defaultRequestURI;

        public Builder(Locale defaultLocale) {
            this.defaultLocale = defaultLocale;
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

            return new RequestURILocaleInterceptor(this);
        }
    }

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
