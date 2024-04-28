# UrlPath Locale Interceptor for Spring

## Dependency
```xml
<dependency>
    <groupId>io.github.alaugks</groupId>
    <artifactId>spring-url-path-locale-interceptor</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## Configuration
```java
import io.github.alaugks.spring.urlpathlocaleinterceptor.UrlPathLocaleInterceptor;
import io.github.alaugks.spring.urlpathlocaleinterceptor.UrlPathLocaleResolver;
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
        UrlPathLocaleResolver resolver = new UrlPathLocaleResolver();
        resolver.setDefaultLocale(this.defaultLocale);
        return resolver;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        UrlPathLocaleInterceptor urlInterceptor = new UrlPathLocaleInterceptor();
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
