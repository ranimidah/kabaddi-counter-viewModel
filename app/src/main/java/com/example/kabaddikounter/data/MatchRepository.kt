package com.example.kabaddikounter.data

class MatchRepository{
    private val api = RetrofitClient.instance

    suspend fun getMatches(): Result<List<Match>> = runCatching {
        api.getMatches()
    }
    suspend fun subscribeToMatch(matchId: String, fcmToken: String): Result<Unit> = runCatching {
        api.subscribeToMatch(matchId, SubscribeRequest(fcmToken))
    }
}