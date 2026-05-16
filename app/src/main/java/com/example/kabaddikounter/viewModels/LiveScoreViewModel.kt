package com.example.kabaddikounter.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.kabaddikounter.data.Match
import com.example.kabaddikounter.data.MatchRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LiveScoreViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MatchRepository()

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

            FirebaseMessaging.getInstance().token.await().let { token ->
                repository.subscribeToMatch(match.id, token)
                    .onSuccess {
                        _subscribedMatch.value = match

                    }
                    .onFailure { _error.value = it.message }
            }
        }
    }


    fun updateSubscribedScore(scoreA: Int, scoreB: Int, status: String) {
        _subscribedMatch.value = _subscribedMatch.value?.copy(
            scoreA = scoreA,
            scoreB = scoreB,
            status = status
        )
    }

    fun resetSubscription() {
        _subscribedMatch.value = null
    }
}