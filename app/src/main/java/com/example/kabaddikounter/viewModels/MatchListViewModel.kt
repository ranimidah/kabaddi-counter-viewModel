package com.example.kabaddikounter.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kabaddikounter.data.Match
import com.example.kabaddikounter.repository.MatchRepository
import com.example.kabaddikounter.service.MatchApiData
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MatchListViewModel(
    private val repository: MatchRepository
) : ViewModel() {

    private val _matches = MutableLiveData<List<MatchApiData>>()
    val matches: LiveData<List<MatchApiData>> = _matches

    private val _subscribedMatch = MutableLiveData<MatchApiData?>()
    val subscribedMatch: LiveData<MatchApiData?> = _subscribedMatch

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadMatches() {
        viewModelScope.launch {
            repository.getMatches()
                .onSuccess {
                    _matches.value = it
                    restoreSubscription()
                }
                .onFailure { _error.value = it.message }
        }
    }

    fun subscribeToMatch(match: MatchApiData) {
        viewModelScope.launch {
            val token = FirebaseMessaging.getInstance().token.await()
            repository.subscribeToMatch(match.id, token)
                .onSuccess { _subscribedMatch.value = match }
                .onFailure { _error.value = it.message }
        }
    }

    fun unsubscribeFromMatch(match: MatchApiData) {
        viewModelScope.launch {
            val token = FirebaseMessaging.getInstance().token.await()
            repository.unsubscribeFromMatch(match.id, token)
                .onSuccess { _subscribedMatch.value = null }
                .onFailure { _error.value = it.message }
        }
    }

    private fun restoreSubscription() {
        viewModelScope.launch {
            val token = FirebaseMessaging.getInstance().token.await()
            val matchList = _matches.value ?: return@launch

            // Cek satu per satu sampai ketemu yang subscribe
            for (match in matchList) {
                repository.checkSubscription(match.id, token)
                    .onSuccess { isSubscribed ->
                        if (isSubscribed) {
                            _subscribedMatch.value = match
                            return@launch  // stop setelah ketemu
                        }
                    }
            }
        }
    }


//    fun updateSubscribedScore(scoreA: Int, scoreB: Int, status: String) {
//        _subscribedMatch.value = _subscribedMatch.value?.copy(
//            score_a = scoreA,
//            score_b = scoreB,
//            status = status
//        )
//    }
//
//    fun resetSubscription() {
//        _subscribedMatch.value = null
//    }
}