package com.example.kabaddikounter.service
data class MatchRequest(
    val team_a: String,
    val team_b: String,
    val score_a: Int,
    val score_b: Int,
    val status: String = "LIVE"
)

data class MatchResponse(
    val success: Boolean,
    val data: MatchResponseData?
)

data class MatchResponseData(
    val id: Int,
    val team_a: String,
    val team_b: String,
    val score_a: Int,
    val score_b: Int,
    val status: String
)

data class LatestMatchResponse(
    val success: Boolean,
    val data: MatchApiData?
)

data class MatchApiData(
    val id: Int,
    val team_a: String,
    val team_b: String,
    val score_a: Int,
    val score_b: Int,
    val status: String,
    val created_at: String
)

data class UpdateScoreRequest(
    val poin_a: Int? = null,  // null jika bukan tim A yang diklik
    val poin_b: Int? = null   // null jika bukan tim B yang diklik
)

//data class SubscribeRequest(val token: String)
data class SubscribeRequest(
    val fcm_token: String
)

data class CheckSubscriptionResponse(
    val is_subscribed: Boolean,
    val data: SubscriberData?
)

data class SubscriberData(
    val id: Int,
    val match_id: Int,
    val fcm_token: String
)
