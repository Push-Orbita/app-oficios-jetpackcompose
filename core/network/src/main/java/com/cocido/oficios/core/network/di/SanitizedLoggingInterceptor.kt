package com.cocido.oficios.core.network.di

import com.cocido.oficios.core.network.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level

/**
 * OkHttp logging with BODY only on debug builds and sensitive fields/headers redacted.
 * Release: not installed (see NetworkModule); level forced to NONE if somehow constructed.
 */
internal class SanitizedLoggingInterceptor : Interceptor {
    private val multipartLogger = SensitiveLogRedactor.MultipartAwareLogger { line ->
        HttpLoggingInterceptor.Logger.DEFAULT.log(line)
    }

    private val delegate = HttpLoggingInterceptor { message ->
        multipartLogger.log(message)
    }.apply {
        level = if (BuildConfig.DEBUG) Level.BODY else Level.NONE
        redactHeader("Authorization")
        redactHeader("Cookie")
        redactHeader("Set-Cookie")
    }

    override fun intercept(chain: Interceptor.Chain): Response = delegate.intercept(chain)
}