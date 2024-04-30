# RequestURI Locale Interceptor for Spring

Handling Locale as first part of [RequestURI](https://jakarta.ee/specifications/servlet/6.0/apidocs/jakarta.servlet/jakarta/servlet/http/httpservletrequest#getRequestURI()). 

Example:

```
HTTP Request                                           RequestURI

https://foo.bar/{locale}/some/path.html     ->         /{locale}/some/path.html
https://foo.bar/{locale}/a.html             ->         /{locale}/a.html
https://foo.bar/{locale}/xyz?a=b            ->         /{locale}/xyz
```

An example in action can be seen [here](https://spring-boot-xliff-example.alaugks.dev/).

## Dependency

### Maven

```xml
<dependency>
    <groupId>io.github.alaugks</groupId>
    <artifactId>spring-requesturi-locale-interceptor</artifactId>
    <version>0.1.0</version>
</dependency>
```

### Gradle

```
implementation group: 'io.github.alaugks', name: 'spring-requesturi-locale-interceptor', version: '0.1.0'
```



## Configuration

### Options

<table>
<thead>
    <tr>
        <th>Options</th>
        <th>Description</th>
        <th>Required</th>
    </tr>
</thead>
<tbody>
    <tr>
        <td>
            defaultLocale(Locale locale)
        </td>
        <td>
            Default and fallback Locale and Locale.
        </td>
        <td>
            Yes
        </td>
    </tr>
    <tr>
        <td>
            supportedLocales(List&lt;Locale&gt; locales)
        </td>
        <td>
            List all locales that are supported.
        </td>
        <td>
            No
        </td>
    </tr>
    <tr>
        <td>
            defaultRequestURI(String path)
        </td>
        <td>
            If the RequestURI is empty, a redirect to the path is performed.
        </td>
        <td>
            No (If not set the default RequestURI is /{defaultLocale}.)
        </td>
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
        RequestURILocaleInterceptor interceptor = RequestURILocaleInterceptor
            .builder()
            .defaultLocale(this.defaultLocale)
            .supportedLocales(this.supportedLocales)
            .defaultRequestURI(
                String.format(
                    "/%s/home",
                    this.defaultLocale.toString()
                )
            )
            .build();

        registry.addInterceptor(urlInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns("/static/**", "/error");
    }
}
```
