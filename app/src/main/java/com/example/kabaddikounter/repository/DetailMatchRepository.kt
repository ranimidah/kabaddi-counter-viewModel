package com.example.kabaddikounter.repository

import com.example.kabaddikounter.data.Match
import com.example.kabaddikounter.data.MatchUpdate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DetailMatchRepository {

    /**
     * Simulate fetching match detail from API/local DB.
     * Replace with your actual data source (Retrofit, Room, etc.)
     */
    fun getMatchDetail(matchId: String): Flow<Match> = flow {
        // Simulate network/db call
        val match = Match(
            id = matchId,
            teamA = "Tim A",
            teamB = "Tim B",
            scoreA = 0,
            scoreB = 1,
            matchTime = "10:23 WIB",
            status = "LIVE"
        )
        emit(match)
    }

    /**
     * Simulate fetching last updates for a match.
     */
    fun getLastUpdates(matchId: String): Flow<List<MatchUpdate>> = flow {
        val updates = listOf(
            MatchUpdate(id = "1", description = "Tim B +1 Poin Serangan"),
            MatchUpdate(id = "2", description = "Tim B +1 Poin Serangan"),
            MatchUpdate(id = "3", description = "Tim B +1 Poin Serangan"),
            MatchUpdate(id = "4", description = "Tim B +1 Poin Serangan")
        )
        emit(updates)
    }

    /**
     * Get subscription status from local preference/storage.
     * Replace with SharedPreferences, DataStore, or your persistence layer.
     */
    fun isSubscribed(matchId: String): Boolean {
        // Stub — implement with DataStore or SharedPreferences
        return false
    }

    /**
     * Save subscription status.
     */
    fun setSubscribed(matchId: String, subscribed: Boolean) {
        // Stub — implement with DataStore or SharedPreferences
    }
}