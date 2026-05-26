// SPDX-License-Identifier: Apache-2.0
// Copyright 2024-2025 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.requesturilocaleinterceptor.mocks;

import java.util.Locale;

public class MockHttpServletRequest extends org.springframework.mock.web.MockHttpServletRequest {

    @Override
    public Locale getLocale() {
        return Locale.forLanguageTag("");
    }
}
