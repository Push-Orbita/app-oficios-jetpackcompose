package com.cocido.oficios.core.network.api

import retrofit2.http.GET
import retrofit2.http.Path

interface DemoApiService {
    @GET("posts/{id}")
    suspend fun getPost(@Path("id") id: Int): GreetingDto
}
