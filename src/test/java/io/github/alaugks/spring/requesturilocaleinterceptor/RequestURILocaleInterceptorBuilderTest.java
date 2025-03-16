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

import io.github.alaugks.spring.requesturilocaleinterceptor.mocks.MockLocaleResolver;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.DispatcherServlet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class RequestURILocaleInterceptorBuilderTest {

    static List<Locale> supportedLocales = new ArrayList<>() {
        {
            add(Locale.forLanguageTag("en"));
            add(Locale.forLanguageTag("de"));
        }
    };
    static Locale defaultLocal = Locale.forLanguageTag("en");
    MockHttpServletRequest mockRequest;
    MockHttpServletResponse mockedResponse;
    MockLocaleResolver mockLocaleResolver;

    @BeforeEach
    void beforeEach() {
        this.mockLocaleResolver = new MockLocaleResolver();
        this.mockRequest = new MockHttpServletRequest();
        this.mockRequest.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, this.mockLocaleResolver);
        this.mockedResponse = new MockHttpServletResponse();
    }

    @Test
    void test_defaultRequestURI_notDefined() {
        this.mockRequest.setRequestURI("/it");
        RequestURILocaleInterceptor interceptor = RequestURILocaleInterceptor
            .builder(defaultLocal)
            .supportedLocales(supportedLocales)
            .build();
        interceptor.preHandle(this.mockRequest, this.mockedResponse, null);

        assertEquals("/en", this.mockedResponse.getRedirectedUrl());
    }

    @Test
    void test_supportedLocales_notDefined() {
        this.mockRequest.setRequestURI("/it");
        RequestURILocaleInterceptor interceptor = RequestURILocaleInterceptor
            .builder(defaultLocal)
            .build();
        interceptor.preHandle(this.mockRequest, this.mockedResponse, null);

        assertEquals("/en", this.mockedResponse.getRedirectedUrl());
    }

    @Test
    void test_defaultLocale_isEmpty() {
        try {
            this.mockRequest.setRequestURI("/it");
            RequestURILocaleInterceptor interceptor = RequestURILocaleInterceptor
                .builder(Locale.forLanguageTag(""))
                .build();
            interceptor.preHandle(this.mockRequest, this.mockedResponse, null);
            fail("Exception not thrown");
        } catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
            assertEquals("Default locale is empty", e.getMessage());
        }
    }
}
