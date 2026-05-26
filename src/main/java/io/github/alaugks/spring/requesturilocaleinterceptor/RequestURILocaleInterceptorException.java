// SPDX-License-Identifier: Apache-2.0
// Copyright 2024-2025 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.requesturilocaleinterceptor;

/**
 * Thrown by {@link RequestURILocaleInterceptor} when the request URI cannot
 * be processed.
 * <p>Wraps the underlying error (for example a missing
 * {@link org.springframework.web.servlet.LocaleResolver LocaleResolver} or a
 * malformed URL) as the cause.
 */
public class RequestURILocaleInterceptorException extends RuntimeException {

    /**
     * Creates a new exception wrapping the given cause.
     *
     * @param cause the underlying error.
     */
    public RequestURILocaleInterceptorException(Throwable cause) {
        super(cause);
    }
}
