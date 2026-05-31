package com.example.kabaddikounter.viewModels

import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kabaddikounter.data.Match
import com.example.kabaddikounter.data.MatchUpdate
import com.example.kabaddikounter.repository.DetailMatchRepository
import kotlinx.coroutines.launch

class DetailMatchViewModel(
    private val repository: DetailMatchRepository = DetailMatchRepository()
) : ViewModel() {

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

    // ── Init ──────────────────────────────────────────────────────────────────

    private var currentMatchId: String = ""

    fun loadMatch(matchId: String) {
        currentMatchId = matchId

        // Restore persisted subscription state
        val subscribed = repository.isSubscribed(matchId)
        _isSubscribed.value = subscribed
        updateSubscribeUi(subscribed)

        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getMatchDetail(matchId).collect { match ->
                    _match.value = match
                    _matchInfo.value =
                        "Pertandingan pukul ${match.matchTime} ${match.teamA} melawan ${match.teamB}"
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
        val current = _isSubscribed.value ?: false
        val newState = !current
        _isSubscribed.value = newState
        repository.setSubscribed(currentMatchId, newState)
        updateSubscribeUi(newState)
    }

    private fun updateSubscribeUi(subscribed: Boolean) {
        if (subscribed) {
            _subscribeStatusText.value = "Notifikasi diaktifkan!"
            _subscribeButtonText.value = "Unsubscribe"
            _subscribeButtonColor.value = Color.parseColor("#E07B2A")   // orange
        } else {
            _subscribeStatusText.value = "Notifikasi belum diaktifkan!"
            _subscribeButtonText.value = "Subscribe"
            _subscribeButtonColor.value = Color.parseColor("#1A12D4")   // blue
        }
    }
}