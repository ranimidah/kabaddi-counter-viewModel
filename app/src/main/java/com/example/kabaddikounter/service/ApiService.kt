package com.example.kabaddikounter.service

import com.example.kabaddikounter.data.Match
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

object RetrofitClient {
    private const val BASE_URL = "http://192.168.1.53:8000/api/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

interface ApiService {
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

    @POST("match/{id}/subscribe")
    suspend fun subscribeToMatch(
        @Path("id") matchId: Int,
        @Body body: SubscribeRequest
    ): Response<Unit>

    @DELETE("match/{id}/unsubscribe")
    suspend fun unsubscribeFromMatch(
        @Path("id") matchId: Int,
        @Query("fcm_token") fcmToken: String
    ): Response<Unit>

    @GET("match/{id}/check-subscription")
    suspend fun checkSubscription(
        @Path("id") matchId: Int,
        @Query("fcm_token") fcmToken: String
    ): Response<CheckSubscriptionResponse>

    @PUT("subscribers/update-token")
    suspend fun updateFcmToken(@Body request: UpdateTokenRequest): Response<Unit>

    @GET("match/{matchId}/detail")
    suspend fun getMatchDetail(@Path("matchId") matchId: Int): Response<MatchDetailResponse>

    @POST("match/{id}/end")
    suspend fun endMatch(@Path("id") matchId: Int): Response<Unit>
}