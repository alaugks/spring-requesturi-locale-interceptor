package io.github.alaugks.spring.urlpathlocaleinterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.util.UriComponentsBuilder;

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
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        try {
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

                path = !path.isEmpty()
                    ? String.format("/%s%s", this.defaultLocale.toString(), path)
                    : this.defaultHomePath;

                response.sendRedirect(this.createUri(request, path).toURL().toString());

                return false;

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
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
