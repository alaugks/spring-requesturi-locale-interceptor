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
import java.util.Locale;
import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.web.servlet.i18n.AbstractLocaleResolver;

public class RequestURILocaleResolver extends AbstractLocaleResolver {

    static final String LOCALE_ATTRIBUTE = RequestURILocaleResolver.class.getName() + ".LOCALE";
    private HttpServletRequest request;

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

    @Override
    public void setLocale(HttpServletRequest request,
        HttpServletResponse response,
        Locale locale
    ) {
        request.setAttribute(LOCALE_ATTRIBUTE, new SimpleLocaleContext(locale).getLocale());
        this.request = request;
    }
}
