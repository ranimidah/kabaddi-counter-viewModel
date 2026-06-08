package com.example.kabaddikounter.repository

import android.content.Context
import com.example.kabaddikounter.data.Match
import com.example.kabaddikounter.data.MatchUpdate
import com.example.kabaddikounter.service.RetrofitClient
import com.example.kabaddikounter.service.SubscribeRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DetailMatchRepository (private val context: Context) {
    private val prefs = context.getSharedPreferences("kabaddi_prefs", Context.MODE_PRIVATE)

    /**
     * Simulate fetching match detail from API/local DB.
     * Replace with your actual data source (Retrofit, Room, etc.)
     */
    fun getMatchDetail(matchId: Int): Flow<Match> = flow {
        val response = RetrofitClient.apiService.getMatchDetail(matchId)
        if (response.isSuccessful) {
            val detail = response.body()?.data ?: throw Exception("Data kosong")
            emit(Match(
                id         = detail.id,
                team_a     = detail.team_a,
                team_b     = detail.team_b,
                score_a    = detail.score_a,
                score_b    = detail.score_b,
                match_time = detail.match_time,
                status     = detail.status
            ))
        } else {
            throw Exception("Gagal memuat detail (${response.code()})")
        }
    }

    /**
     * Simulate fetching last updates for a match.
     */
    fun getLastUpdates(matchId: Int): Flow<List<MatchUpdate>> = flow {
        val response = RetrofitClient.apiService.getMatchDetail(matchId)
        if (response.isSuccessful) {
            val updates = response.body()?.data?.last_updates ?: emptyList()
            emit(updates.map { log ->
                MatchUpdate(
                    team    = log.team,
                    points  = log.points,
                    score_a = log.score_a,
                    score_b = log.score_b,
                    time    = log.time
                )
            })
        } else {
            throw Exception("Gagal memuat log (${response.code()})")
        }
    }

    /**
     * Get subscription status from local preference/storage.
     * Replace with SharedPreferences, DataStore, or your persistence layer.
     */
    fun isSubscribed(matchId: Int): Boolean {
        // Baca dari SharedPreferences sebagai cache lokal
        return prefs.getBoolean("subscribed_$matchId", false)
    }

    /**
     * Save subscription status.
     */
    fun setSubscribed(matchId: Int, subscribed: Boolean) {
        prefs.edit().putBoolean("subscribed_$matchId", subscribed).apply()
    }

    suspend fun checkSubscription(
        matchId: Int,
        token: String
    ): Result<Boolean> = try {

        val res = RetrofitClient.apiService.checkSubscription(matchId, token)

        if (res.isSuccessful) {
            Result.success(res.body()?.is_subscribed ?: false)
        } else {
            Result.failure(Exception("Gagal cek: ${res.code()}"))
        }

    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun subscribeToMatch(
        matchId: Int,
        token: String
    ): Result<Unit> = try {

        val res = RetrofitClient.apiService
            .subscribeToMatch(matchId, SubscribeRequest(token))

        if (res.isSuccessful)
            Result.success(Unit)
        else
            Result.failure(Exception("Subscribe gagal"))

    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun unsubscribeFromMatch(
        matchId: Int,
        token: String
    ): Result<Unit> = try {

        val res = RetrofitClient.apiService
            .unsubscribeFromMatch(matchId, token)

        if (res.isSuccessful)
            Result.success(Unit)
        else
            Result.failure(Exception("Unsubscribe gagal"))

    } catch (e: Exception) {
        Result.failure(e)
    }
}