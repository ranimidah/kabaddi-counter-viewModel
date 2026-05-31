package com.example.kabaddikounter.service

import com.example.kabaddikounter.data.Match
import com.example.kabaddikounter.data.SubscribeRequest
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

object RetrofitClient {
    private const val BASE_URL = "http://192.168.0.12:8000/api/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

interface ApiService {
    @GET("match")
    suspend fun getMatches(): List<Match>

    @POST("match/{id}/subscribe")
    suspend fun subscribeToMatch(
        @Path("id") matchId: String,
        @Body body: SubscribeRequest
    ): Response<Unit>

    @POST("match/save")
    suspend fun saveMatch(@Body body: MatchRequest): Response<MatchResponse>

    @POST("score-logs")
    suspend fun saveScoreLog(@Body body: ScoreLogRequest): Response<ScoreLogResponse>

    @GET("match")
    suspend fun getMatchHistory(): Response<List<MatchApiData>>

    @GET("match/latest")
    suspend fun getLatestMatch(): Response<LatestMatchResponse>

    @PUT("match/{id}/score")
    suspend fun updateScore(
        @Path("id") matchId: Int,
        @Body body: UpdateScoreRequest
    ): Response<Unit>

    @POST("matches/{matchId}/subscribe")
    suspend fun subscribeToMatch(
        @Path("matchId") matchId: Int,
        @Body body: SubscribeRequest
    ): Response<Unit>

    @DELETE("matches/{matchId}/subscribe")
    suspend fun unsubscribeFromMatch(
        @Path("matchId") matchId: Int,
        @Body body: SubscribeRequest
    ): Response<Unit>
}