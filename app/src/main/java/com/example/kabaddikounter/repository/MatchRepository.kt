package com.example.kabaddikounter.repository

import android.content.Context
import com.example.kabaddikounter.data.Match
import com.example.kabaddikounter.service.LiveScoreForegroundService
import com.example.kabaddikounter.service.MatchApiData
import com.example.kabaddikounter.service.RetrofitClient
import com.example.kabaddikounter.service.SubscribeRequest
import com.example.kabaddikounter.service.UpdateTokenRequest

class MatchRepository(private val context: Context) {
    private val api = RetrofitClient.apiService

    suspend fun getMatches(): Result<List<MatchApiData>> = try {
        val res = api.getMatchHistory()
        if (res.isSuccessful) Result.success(res.body() ?: emptyList())
        else Result.failure(Exception("Gagal memuat: ${res.code()}"))
    } catch (e: Exception) {
        Result.failure(e)
    }
//    suspend fun subscribeToMatch(matchId: String, fcmToken: String): Result<Unit> = runCatching {
//        api.subscribeToMatch(matchId, SubscribeRequest(fcmToken))
//    }
    suspend fun subscribeToMatch(matchId: Int, fcm_token: String): Result<Unit> = try {
        val res = api.subscribeToMatch(matchId, SubscribeRequest(fcm_token))
        if (res.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Subscribe gagal: ${res.code()}"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun unsubscribeFromMatch(matchId: Int, token: String): Result<Unit> = try {
        val res = api.unsubscribeFromMatch(matchId, token)  // ← wrap ke SubscribeRequest
        if (res.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Unsubscribe gagal: ${res.code()}"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun checkSubscription(matchId: Int, token: String): Result<Boolean> = try {
        val res = api.checkSubscription(matchId, token)
        if (res.isSuccessful) Result.success(res.body()?.is_subscribed ?: false)
        else Result.failure(Exception("Gagal cek: ${res.code()}"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateFcmToken(oldToken: String, newToken: String): Result<Unit> = try {
        val res = api.updateFcmToken(UpdateTokenRequest(oldToken, newToken))
        if (res.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Update token gagal: ${res.code()}"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun updateScore(match: Match) {
        LiveScoreForegroundService.start(context, match.team_a, match.team_b, match.score_a, match.score_b)
    }

    fun subscribe(matchId: String) {
        // TODO: panggil ApiService POST /subscribe
    }

    fun stop() {
        LiveScoreForegroundService.stop(context)
    }
}