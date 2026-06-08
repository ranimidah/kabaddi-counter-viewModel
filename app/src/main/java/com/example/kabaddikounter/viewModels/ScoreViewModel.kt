package com.example.kabaddikounter.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import android.app.Application
import androidx.lifecycle.asLiveData
import com.example.kabaddikounter.data.ThemePreferences
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.kabaddikounter.data.AppDatabase
import com.example.kabaddikounter.data.MatchEntity
import com.example.kabaddikounter.data.ScoreLogEntity
import com.example.kabaddikounter.service.MatchApiData
import com.example.kabaddikounter.service.MatchRequest
import com.example.kabaddikounter.service.RetrofitClient
import com.example.kabaddikounter.service.ScoreLogRequest
import com.example.kabaddikounter.service.UpdateScoreRequest

class ScoreViewModel(application: Application) : AndroidViewModel(application) {
    val teamA = MutableLiveData("Team A")
    val teamB = MutableLiveData("Team B")

    val textExample = MutableLiveData<String>("test")

    private val _scoreA = MutableLiveData<Int>(0)
    val scoreA: LiveData<Int>
        get() = _scoreA

    private val _scoreB = MutableLiveData<Int>(0)
    val scoreB: LiveData<Int>
        get() = _scoreB

    // subscribe
    private val _isSubscribed = MutableLiveData(false)
    val isSubscribed: LiveData<Boolean> = _isSubscribed

    // thema dark/light
    private val prefs = ThemePreferences(application)

    // room data
    private val db = AppDatabase.getInstance(application)
    private val matchDao = db.matchDao()
    private val scoreLogDao = db.scoreLogDao()
    val allMatches = matchDao.getAllMatches()

    // Status simpan untuk feedback ke UI
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

    fun setSubscribed(subscribed: Boolean) {
        _isSubscribed.value = subscribed
    }

    fun setLiveMatchData(teamA: String, teamB: String, scoreA: Int, scoreB: Int) {
        this.teamA.value = teamA
        this.teamB.value = teamB
        _scoreA.value = scoreA
        _scoreB.value = scoreB
    }

    // score subsribe
    fun applyLiveScore(teamA: String, teamB: String, scoreA: Int, scoreB: Int) {
        this.teamA.value = teamA
        this.teamB.value = teamB
        _scoreA.value = scoreA
        _scoreB.value = scoreB
        _isSubscribed.value = true
    }

    // subscribe reset
    fun resetToDefault() {
        _isSubscribed.value = false
        reset()
        teamA.value = "Team A"
        teamB.value = "Team B"
    }


    // Di ScoreViewModel.kt, ganti:
    val isDarkModeLive: LiveData<Boolean> = prefs.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
        .asLiveData()

    fun toggleTheme(isDark: Boolean) {
        viewModelScope.launch {
            prefs.saveTheme(isDark)
        }
    }

    // Room Database
    val saveStatus: LiveData<String?> get() = _saveStatus
    fun saveMatch(title: String) {
        viewModelScope.launch {
            val existing = _currentMatch.value

            if (existing != null && existing.status == "LIVE") {
                // Update skor match yang sudah ada
                matchDao.updateScore(existing.id, _scoreA.value ?: 0, _scoreB.value ?: 0)
                _currentMatch.postValue(existing.copy(
                    score_a = _scoreA.value ?: 0,
                    score_b = _scoreB.value ?: 0
                ))
                _saveStatus.postValue("Skor \"${existing.title}\" berhasil diperbarui!")

                // Update ke server juga
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
                        // Tetap berhasil lokal, tapi server gagal
                        _saveStatus.postValue("Tersimpan lokal, gagal ke server (${response.code()})")
                    }
                } catch (e: Exception) {
                    // Misal tidak ada koneksi internet
                    _saveStatus.postValue("Tersimpan lokal, server tidak terjangkau")
                }
            }
        }
    }

    fun endMatch() {
        viewModelScope.launch {
            val existing = _currentMatch.value ?: return@launch

            matchDao.updateStatus(existing.id, "END")
            _currentMatch.postValue(existing.copy(status = "END"))

            try {
                val response = RetrofitClient.apiService.endMatch(existing.id)
                if (response.isSuccessful) {
                    _saveStatus.postValue("Match selesai!")
                } else {
                    _saveStatus.postValue("Match selesai (lokal), gagal ke server (${response.code()})")
                }
            } catch (e: Exception) {
                _saveStatus.postValue("Match selesai (lokal), server tidak terjangkau")
            }

            // Reset semua state
            _currentMatch.postValue(null)
            _scoreA.postValue(0)
            _scoreB.postValue(0)
            teamA.postValue("Team A")
            teamB.postValue("Team B")
        }
    }

    fun clearSaveStatus() { _saveStatus.value = null }

    suspend fun getAllMatches(): List<MatchEntity> {
        return matchDao.getAllMatchesOnce()
    }

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

    // Load match terakhir saat ViewModel dibuat
    init {
        loadLastMatch()
    }
    fun loadLastMatch() {
        viewModelScope.launch {
            val localMatch = matchDao.getLastMatch()
            android.util.Log.d("LOAD_MATCH", "Local: $localMatch")

            if (localMatch != null) {
                applyMatchToUI(localMatch)
            }

            try {
                val response = RetrofitClient.apiService.getLatestMatch()
                android.util.Log.d("LOAD_MATCH", "API code: ${response.code()}")

                if (response.isSuccessful) {
                    val apiMatch = response.body()?.data
                    android.util.Log.d("LOAD_MATCH", "API data: $apiMatch")

                    if (apiMatch != null) {
                        // Simpan/update ke SQLite lokal
                        val entity = MatchEntity(
                            id = apiMatch.id,
                            title = "${apiMatch.team_a} vs ${apiMatch.team_b}",
                            team_a = apiMatch.team_a,
                            team_b = apiMatch.team_b,
                            score_a = apiMatch.score_a,
                            score_b = apiMatch.score_b,
                            status = apiMatch.status
                        )
                        matchDao.insertOrReplace(entity) // pakai upsert
                        applyMatchToUI(entity)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("LOAD_MATCH", "API error: ${e.message}")
                // Tidak apa-apa, sudah pakai data lokal
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
            // Update SQLite lokal
            matchDao.updateScore(match.id, _scoreA.value ?: 0, _scoreB.value ?: 0)
            _currentMatch.postValue(match.copy(
                score_a = _scoreA.value ?: 0,
                score_b = _scoreB.value ?: 0
            ))

            // Kirim ke API
            try {
                val request = UpdateScoreRequest(poin_a = poinA, poin_b = poinB)
                val response = RetrofitClient.apiService.updateScore(match.id, request)
                if (response.isSuccessful) {
                    android.util.Log.d("UPDATE_SCORE", "Berhasil: poin_a=$poinA poin_b=$poinB")
                } else {
                    android.util.Log.e("UPDATE_SCORE", "Gagal ${response.code()}: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                android.util.Log.e("UPDATE_SCORE", "Error: ${e.message}")
            }
        }
    }
}
