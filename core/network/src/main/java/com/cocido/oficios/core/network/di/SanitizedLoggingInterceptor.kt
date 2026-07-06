package com.cocido.oficios.core.network.di

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor

internal class SanitizedLoggingInterceptor : Interceptor {
    private val delegate = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
        redactHeader("Authorization")
    }

    override fun intercept(chain: Interceptor.Chain): Response = delegate.intercept(chain)
}
