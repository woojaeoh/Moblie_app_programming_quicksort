package com.example.quicksort

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AiApi {

    @GET("ping")
    suspend fun ping(): Response<Map<String, String>>

    @POST("predict")
    suspend fun predict(@Body body: AiRequest): Response<AiResponse>
}