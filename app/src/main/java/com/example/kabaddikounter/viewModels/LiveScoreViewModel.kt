package com.example.kabaddikounter.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.kabaddikounter.data.Match
import com.example.kabaddikounter.repository.MatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LiveScoreViewModel(
    private val repository: MatchRepository  // ← Context dihapus, pakai Repository
) : ViewModel() {

    // --- Score State ---
    private val _scoreState = MutableLiveData(Match())
    val scoreState: LiveData<Match> = _scoreState

    // --- Subscribe State ---
    private val _isSubscribed = MutableStateFlow(false)          // ← deklarasi yang hilang
    val isSubscribedLiveData: LiveData<Boolean> = _isSubscribed  // ← kini tersedia
        .asStateFlow()
        .asLiveData()

    // --- Actions ---

    fun updateScore(teamA: String, teamB: String, scoreA: Int, scoreB: Int) {
        viewModelScope.launch {
            val newState = Match(
                team_a  = teamA,
                team_b  = teamB,
                score_a = scoreA,
                score_b = scoreB,
                status = "LIVE"
                // id dihapus dari sini — sebaiknya ID dari server/data source, bukan random tiap update
            )
            _scoreState.value = newState
            repository.updateScore(newState)  // ← Service dipanggil dari Repository
        }
    }

    fun subscribe(matchId: String) {
        viewModelScope.launch {
            repository.subscribe(matchId)
            _isSubscribed.value = true  // ← tombol di-disable setelah subscribe
        }
    }

    fun stopLiveScore() {
        viewModelScope.launch {
            _scoreState.value = _scoreState.value?.copy(status = "END")
            _isSubscribed.value = false
            repository.stop()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopLiveScore()
    }
}