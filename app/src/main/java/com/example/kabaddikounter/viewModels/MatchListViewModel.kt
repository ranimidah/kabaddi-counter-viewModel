package com.example.kabaddikounter.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kabaddikounter.data.Match
import com.example.kabaddikounter.repository.MatchRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MatchListViewModel(
    private val repository: MatchRepository
) : ViewModel() {

    private val _matches = MutableLiveData<List<Match>>()
    val matches: LiveData<List<Match>> = _matches

    private val _subscribedMatch = MutableLiveData<Match?>()
    val subscribedMatch: LiveData<Match?> = _subscribedMatch

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadMatches() {
        viewModelScope.launch {
            repository.getMatches()
                .onSuccess { _matches.value = it }
                .onFailure { _error.value = it.message }
        }
    }

    fun subscribeToMatch(match: Match) {
        viewModelScope.launch {
            val token = FirebaseMessaging.getInstance().token.await()
            repository.subscribeToMatch(match.id, token)
                .onSuccess { _subscribedMatch.value = match }
                .onFailure { _error.value = it.message }
        }
    }

    fun updateSubscribedScore(scoreA: Int, scoreB: Int, status: String) {
        _subscribedMatch.value = _subscribedMatch.value?.copy(
            score_a = scoreA,
            score_b = scoreB,
            status = status
        )
    }

    fun resetSubscription() {
        _subscribedMatch.value = null
    }
}