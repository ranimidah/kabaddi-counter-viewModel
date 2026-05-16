package com.example.kabaddikounter.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @GET("match")
    suspend fun getMatches(): List<Match>

    @POST("match/{id}/subscribe")
    suspend fun subscribeToMatch(
        @Path("id") matchId: String,
        @Body body: SubscribeRequest
    ): retrofit2.Response<Unit>
}

object RetrofitClient {
    private const val BASE_URL = "http://YOUR_SERVER_IP:PORT/"

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}