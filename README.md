# RequestURI Locale Interceptor for Spring

Handling Locale (i18n) as first part of RequestURI. 

Example RequestURI:
```
/{locale}/example/path?param=value
```

An [example](https://spring-boot-xliff-example.alaugks.dev/) in action can be viewed here.

## Dependency
```xml
<dependency>
    <groupId>io.github.alaugks</groupId>
    <artifactId>spring-requesturi-locale-interceptor</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Configuration

### Options

<table>
<thead>
    <tr>
        <th align="left">Options</th>
        <th align="left">Description</th>
    </tr>
</thead>
<tbody>
    <tr>
        <td align="left">setSupportedLocales(Locale locale)</td>
        <td align="left">Default Locale and Locale fallback.</td>
    </tr>
    <tr>
        <td align="left">setDefaultLocale(List<Locale> locales)</td>
        <td align="left">List all locales that are supported.</td>
    </tr>
    <tr>
        <td align="left">setDefaultHomePath(String path)</td>
        <td align="left">If a RequestURI is not exists (empty), a redirect to the path is performed.</td>
    </tr>
</tbody>
</table>

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
        RequestURILocaleInterceptor urlInterceptor = new RequestURILocaleInterceptor();
        urlInterceptor.setDefaultLocale(this.defaultLocale);
        urlInterceptor.setSupportedLocales(this.supportedLocales);
        urlInterceptor.setDefaultHomePath(
            String.format(
                "/%s/home",
                this.defaultLocale.toString()
            )
        );

        registry.addInterceptor(urlInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns("/static/**", "/error");
    }
}
```
