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

package io.github.alaugks.spring.requesturilocaleinterceptor.mocks;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Locale;
import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.web.servlet.LocaleResolver;

public class MockLocaleResolver implements LocaleResolver {

    static final String MOCK_LOCALE_ATTRIBUTE = "LOCALE";
    HttpServletRequest request;
    HttpServletResponse response;

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        return (Locale) this.request.getAttribute(MOCK_LOCALE_ATTRIBUTE);
    }

    @Override
    public void setLocale(HttpServletRequest request,
        HttpServletResponse response,
        Locale locale
    ) {
        request.setAttribute(MOCK_LOCALE_ATTRIBUTE, new SimpleLocaleContext(locale).getLocale());
        this.request = request;
        this.response = response;
    }
}
