# RequestURI Locale Interceptor for Spring

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=alaugks_spring-requesturi-locale-interceptor&metric=alert_status)](https://sonarcloud.io/summary/overall?id=alaugks_spring-requesturi-locale-interceptor)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.alaugks/spring-requesturi-locale-interceptor.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.alaugks/spring-requesturi-locale-interceptor/0.3.1)

Handling Locale as first part of [RequestURI](https://jakarta.ee/specifications/servlet/6.0/apidocs/jakarta.servlet/jakarta/servlet/http/httpservletrequest#getRequestURI()). 

Example:

| HTTP Request                              | RequestURI                 |
|-------------------------------------------|----------------------------|
| `https://foo.bar/{locale}/some/path.html` | `/{locale}/some/path.html` |
| `https://foo.bar/{locale}/a.html`         | `/{locale}/a.html`         |
| `https://foo.bar/{locale}/xyz?a=b`        | `/{locale}/xyz`            |

## Locale Resolution

The first path segment of the RequestURI determines the `java.util.Locale` that Spring uses for the request
(e.g. `LocaleContextHolder.getLocale()`, `MessageSource`, `RequestContextUtils.getLocale(request)`).

* If the first segment is one of the `supportedLocales`, that locale from the URL becomes the `Locale` of the request.
* If it is missing or not supported, the request is redirected to the same path prefixed with the `defaultLocale`
  (or to `defaultRequestURI` if no path remains). The `Locale` of the request is then the `defaultLocale`.

| RequestURI              | Redirect to                    | `Locale`               |
|-------------------------|--------------------------------|------------------------|
| `/en/some/path.html`    | -                              | `en`                   |
| `/en-us/some/path.html` | -                              | `en-US`                |
| `/de/some/path.html`    | -                              | `de`                   |
| `/fr/some/path.html`    | `/en/some/path.html`           | `en` (`defaultLocale`) |
| `/some/path.html`       | `/en/path.html`                | `en` (`defaultLocale`) |
| `/`                     | `/en` (or `defaultRequestURI`) | `en` (`defaultLocale`) |

The examples assume `supportedLocales` are `en`, `en-US` and `de`, and `defaultLocale` is `en`.

## Dependency

### Maven

```xml
<dependency>
    <groupId>io.github.alaugks</groupId>
    <artifactId>spring-requesturi-locale-interceptor</artifactId>
    <version>0.3.1</version>
</dependency>
```

### Gradle

```
implementation group: 'io.github.alaugks', name: 'spring-requesturi-locale-interceptor', version: '0.3.1'
```

## Configuration

`builder(Locale defaultLocale)` (***required***)
* Argument `Locale defaultLocale`: Defines the default and fallback locale.

`supportedLocales(List<Locale> locales)`
* Lists all locales that are supported.

`defaultRequestURI(String path)`
* Defines the path to redirect to if the RequestURI is empty. If not set, the default RequestURI is `/{defaultLocale}`.

### Spring Configuration

```java
import io.github.alaugks.spring.requesturilocaleinterceptor.RequestURILocaleInterceptor;
import io.github.alaugks.spring.requesturilocaleinterceptor.RequestURILocaleResolver;
import java.util.List;
import java.util.Locale;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfigurerConfig implements WebMvcConfigurer {

    private final Locale defaultLocale = Locale.forLanguageTag("en");

    private final List<Locale> supportedLocales = List.of(
        Locale.forLanguageTag("en"),
        Locale.forLanguageTag("de"),
        Locale.forLanguageTag("en-US")
    );

    @Bean
    public LocaleResolver localeResolver() {
        RequestURILocaleResolver resolver = new RequestURILocaleResolver();
        resolver.setDefaultLocale(this.defaultLocale);
        return resolver;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        RequestURILocaleInterceptor interceptor = RequestURILocaleInterceptor
            .builder(this.defaultLocale)
            .supportedLocales(this.supportedLocales)
            .defaultRequestURI("/en/home")
            .build();
        
        registry.addInterceptor(urlInterceptor)
            .addPathPatterns("/**")
            // Exclude from Interceptor
            .excludePathPatterns("/static/**", "/error");
    }
}
```

## Example

A complete, runnable Spring Boot application is available in the
[spring-messagesource-xliff-example](https://github.com/alaugks/spring-messagesource-xliff-example) repository.
It shows this interceptor in action, resolving the locale from the first path segment of the request URI
(e.g. `/en/...`, `/de/...`).
