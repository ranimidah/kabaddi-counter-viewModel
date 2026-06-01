package com.example.kabaddikounter.viewModels

import android.app.Application
import android.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.kabaddikounter.data.Match
import com.example.kabaddikounter.data.MatchUpdate
import com.example.kabaddikounter.repository.DetailMatchRepository
import com.example.kabaddikounter.service.RetrofitClient
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DetailMatchViewModel (application: Application) : AndroidViewModel(application) {
    private val repository: DetailMatchRepository = DetailMatchRepository(application)
    // ── Match Data ────────────────────────────────────────────────────────────

    private val _match = MutableLiveData<Match>()
    val match: LiveData<Match> get() = _match

    private val _lastUpdates = MutableLiveData<List<MatchUpdate>>()
    val lastUpdates: LiveData<List<MatchUpdate>> get() = _lastUpdates

    // ── Derived display strings (used in DataBinding expressions) ─────────────

    val matchInfo: LiveData<String> get() = _matchInfo
    private val _matchInfo = MutableLiveData<String>()

    // ── Subscribe State ───────────────────────────────────────────────────────

    private val _isSubscribed = MutableLiveData<Boolean>()
    val isSubscribed: LiveData<Boolean> get() = _isSubscribed

    /** Label text shown below "Subscribe untuk Live Pertandingan" */
    val subscribeStatusText: LiveData<String> get() = _subscribeStatusText
    private val _subscribeStatusText = MutableLiveData<String>()

    /** Button label — "Subscribe" or "Unsubscribe" */
    val subscribeButtonText: LiveData<String> get() = _subscribeButtonText
    private val _subscribeButtonText = MutableLiveData<String>()

    /**
     * Button background color as int.
     * Blue  (#0000EE-ish) when not subscribed.
     * Orange (#E07B2A-ish) when subscribed (Unsubscribe).
     */
    val subscribeButtonColor: LiveData<Int> get() = _subscribeButtonColor
    private val _subscribeButtonColor = MutableLiveData<Int>()

    // ── Loading / Error ───────────────────────────────────────────────────────

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    val matchStatusText: LiveData<String> = _match.map { match ->
        if (match.status == "LIVE") "LIVE MATCH" else "END MATCH"
    }

    val matchStatusColor: LiveData<Int> = _match.map { match ->
        if (match.status == "LIVE") Color.parseColor("#E53935") // merah
        else Color.parseColor("#757575") // abu-abu
    }

    // ── Init ──────────────────────────────────────────────────────────────────

    private var currentMatchId: Int = -1

    fun loadMatch(matchId: Int) {
        currentMatchId = matchId

        // Restore persisted subscription state
//        val subscribed = repository.isSubscribed(matchId)
//        _isSubscribed.value = subscribed
//        updateSubscribeUi(subscribed)


        viewModelScope.launch {
            val token = FirebaseMessaging.getInstance().token.await()
            repository.checkSubscription(matchId, token)
                .onSuccess { subscribed ->
                    _isSubscribed.value = subscribed
                    updateSubscribeUi(subscribed)
                }.onFailure {
                    _isSubscribed.value = false
                    updateSubscribeUi(false)
                }


            _isLoading.value = true
            try {
                repository.getMatchDetail(matchId).collect { match ->
                    _match.value = match
                    _matchInfo.value =
                        "Pertandingan pukul ${match.match_time} ${match.team_a} melawan ${match.team_b}"
                }
                repository.getLastUpdates(matchId).collect { updates ->
                    _lastUpdates.value = updates
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ── Subscribe Toggle ──────────────────────────────────────────────────────

    fun toggleSubscribe() {
//        val current = _isSubscribed.value ?: false
//        val newState = !current
//        _isSubscribed.value = newState
//        repository.setSubscribed(currentMatchId, newState)
//        updateSubscribeUi(newState)
        viewModelScope.launch {

            val token = FirebaseMessaging.getInstance().token.await()
            val current = _isSubscribed.value ?: false

            if (current) {

                repository.unsubscribeFromMatch(currentMatchId, token)
                    .onSuccess {
                        _isSubscribed.value = false
                        updateSubscribeUi(false)
                    }

            } else {

                repository.subscribeToMatch(currentMatchId, token)
                    .onSuccess {
                        _isSubscribed.value = true
                        updateSubscribeUi(true)
                    }
            }
        }
    }

    private fun updateSubscribeUi(subscribed: Boolean) {
        if (subscribed) {
            _subscribeStatusText.value = "Unsubscribe untuk mengakhiri live match \n notifikasi telah diaktifkan!"
            _subscribeButtonText.value = "Unsubscribe"
            _subscribeButtonColor.value = Color.parseColor("#E07B2A")   // orange
        } else {
            _subscribeStatusText.value = "Subscribe untuk live pertandingan \n notifikasi belum diaktifkan!"
            _subscribeButtonText.value = "Subscribe"
            _subscribeButtonColor.value = Color.parseColor("#1A12D4")   // blue
        }
    }

}