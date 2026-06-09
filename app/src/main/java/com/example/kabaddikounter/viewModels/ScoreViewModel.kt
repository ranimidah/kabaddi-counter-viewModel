package com.example.kabaddikounter.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.app.Application
import com.example.kabaddikounter.MatchListWidget
import com.example.kabaddikounter.data.ThemePreferences
import kotlinx.coroutines.launch
import com.example.kabaddikounter.data.AppDatabase
import com.example.kabaddikounter.data.MatchEntity
import com.example.kabaddikounter.service.MatchApiData
import com.example.kabaddikounter.service.MatchRequest
import com.example.kabaddikounter.service.RetrofitClient
import com.example.kabaddikounter.service.UpdateScoreRequest

class ScoreViewModel(application: Application) : AndroidViewModel(application) {
    val teamA = MutableLiveData("Team A")
    val teamB = MutableLiveData("Team B")

    val textExample =  MutableLiveData<String>("test")

    private val _scoreA = MutableLiveData<Int>(0)
    val scoreA: LiveData<Int>
        get() = _scoreA

    private val _scoreB = MutableLiveData<Int>(0)
    val scoreB: LiveData<Int>
        get() = _scoreB

    // subscribe
    private val _isSubscribed = MutableLiveData(false)
    val isSubscribed: LiveData<Boolean> = _isSubscribed

    private val _isDataReady = MutableLiveData<Boolean>(false)
    val isDataReady: MutableLiveData<Boolean> = _isDataReady

    // thema dark/light
    private val prefs = ThemePreferences(application)

    // room data
    private val db = AppDatabase.getInstance(application)
    private val matchDao = db.matchDao()
    private val scoreLogDao = db.scoreLogDao()
    val allMatches = matchDao.getAllMatches()

    private val _saveStatus = MutableLiveData<String?>()

    private val _currentMatch = MutableLiveData<MatchEntity?>()
    val currentMatch: LiveData<MatchEntity?> get() = _currentMatch

    fun incrementScoreA(points: Int = 1) {
        _scoreA.value = (_scoreA.value ?: 0) + points
        updateCurrentMatchScore(poinA = points, poinB = null)
    }

    fun incrementScoreB(points: Int = 1) {
        _scoreB.value = (_scoreB.value ?: 0) + points
        updateCurrentMatchScore(poinA = null, poinB = points)
    }

    fun reset() {
        _scoreA.value = 0
        _scoreB.value = 0
        teamA.value = "Team A"
        teamB.value = "Team B"
    }



    val saveStatus: LiveData<String?> get() = _saveStatus
    fun saveMatch(title: String) {
        viewModelScope.launch {
            val existing = _currentMatch.value
            if (existing != null && existing.status == "LIVE") {
                matchDao.updateScore(existing.id, _scoreA.value ?: 0, _scoreB.value ?: 0)
                _currentMatch.postValue(existing.copy(
                    score_a = _scoreA.value ?: 0,
                    score_b = _scoreB.value ?: 0
                ))
                _saveStatus.postValue("Skor \"${existing.title}\" berhasil diperbarui!")

                try {
                    val request = MatchRequest(
                        team_a = existing.team_a,
                        team_b = existing.team_b,
                        score_a = _scoreA.value ?: 0,
                        score_b = _scoreB.value ?: 0,
                        status = "LIVE"
                    )
                    RetrofitClient.apiService.saveMatch(request)
                } catch (e: Exception) { }
            } else {
                val match = MatchEntity(
                    title = title,
                    team_a = teamA.value ?: "Team A",
                    team_b = teamB.value ?: "Team B",
                    score_a = _scoreA.value ?: 0,
                    score_b = _scoreB.value ?: 0
                )
                matchDao.insertMatch(match)
                try {
                    val request = MatchRequest(
                        team_a = match.team_a,
                        team_b = match.team_b,
                        score_a = match.score_a,
                        score_b = match.score_b,
                        status = "LIVE"
                    )
                    val response = RetrofitClient.apiService.saveMatch(request)
                    if (response.isSuccessful) {
                        _saveStatus.postValue("Match \"$title\" berhasil disimpan!")
                    } else {
                        _saveStatus.postValue("Tersimpan lokal, gagal ke server (${response.code()})")
                    }
                } catch (e: Exception) {
                    _saveStatus.postValue("Tersimpan lokal, server tidak terjangkau")
                }
            }
            MatchListWidget.sendRefreshBroadcast(getApplication())
        }
    }

    fun endMatch() {
        viewModelScope.launch {
            val existing = _currentMatch.value ?: return@launch
            matchDao.updateStatus(existing.id, "END")
            try {
                RetrofitClient.apiService.endMatch(existing.id)
                _saveStatus.postValue("Match selesai!")
            } catch (e: Exception) {
                _saveStatus.postValue("Match selesai (lokal), server tidak terjangkau")
            }
            _currentMatch.postValue(null)
            _scoreA.postValue(0)
            _scoreB.postValue(0)
            teamA.postValue("Team A")
            teamB.postValue("Team B")
            MatchListWidget.sendRefreshBroadcast(getApplication())
        }
    }

    fun clearSaveStatus() { _saveStatus.value = null }

    suspend fun getAllMatches(): List<MatchEntity> = matchDao.getAllMatchesOnce()

    // get history matchs
    private val _matchHistory = MutableLiveData<List<MatchApiData>>()
    val matchHistory: LiveData<List<MatchApiData>> get() = _matchHistory

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _fetchError = MutableLiveData<String?>()
    val fetchError: LiveData<String?> get() = _fetchError

    fun fetchMatchHistory() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val response = RetrofitClient.apiService.getMatchHistory()
                if (response.isSuccessful) {
                    _matchHistory.postValue(response.body() ?: emptyList()) // hapus ?.data
                } else {
                    _fetchError.postValue("Gagal memuat data (${response.code()})")
                }
            } catch (e: Exception) {
                _fetchError.postValue("Tidak dapat terhubung ke server")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    init {
        loadLastMatch()
    }

    fun loadLastMatch() {
        viewModelScope.launch {
            val localMatch = matchDao.getLastMatch()
            if (localMatch != null && localMatch.status == "LIVE") {
                applyMatchToUI(localMatch)
            }

            try {
                val response = RetrofitClient.apiService.getLatestMatch()
                if (response.isSuccessful) {
                    val apiMatch = response.body()?.data
                    if (apiMatch != null) {
                        val entity = MatchEntity(
                            id = apiMatch.id,
                            title = "${apiMatch.team_a} vs ${apiMatch.team_b}",
                            team_a = apiMatch.team_a,
                            team_b = apiMatch.team_b,
                            score_a = apiMatch.score_a,
                            score_b = apiMatch.score_b,
                            status = apiMatch.status
                        )
                        matchDao.insertOrReplace(entity)
                        if (apiMatch.status == "LIVE") {
                            applyMatchToUI(entity)
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("LOAD_MATCH", "API error: ${e.message}")
            }
        }
    }

    private fun applyMatchToUI(match: MatchEntity) {
        _currentMatch.postValue(match)
        teamA.postValue(match.team_a)
        teamB.postValue(match.team_b)
        _scoreA.postValue(match.score_a)
        _scoreB.postValue(match.score_b)
    }

    private fun updateCurrentMatchScore(poinA: Int? = null, poinB: Int? = null) {
        val match = _currentMatch.value ?: return
        viewModelScope.launch {
            matchDao.updateScore(match.id, _scoreA.value ?: 0, _scoreB.value ?: 0)
            _currentMatch.postValue(match.copy(
                score_a = _scoreA.value ?: 0,
                score_b = _scoreB.value ?: 0
            ))
            try {
                val request = UpdateScoreRequest(poin_a = poinA, poin_b = poinB)
                RetrofitClient.apiService.updateScore(match.id, request)
            } catch (e: Exception) { }
            MatchListWidget.sendRefreshBroadcast(getApplication())
        }
    }
}
