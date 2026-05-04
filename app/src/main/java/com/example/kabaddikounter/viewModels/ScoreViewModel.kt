package com.example.kabaddikounter.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import android.app.Application
import com.example.kabaddikounter.data.ThemePreferences
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.kabaddikounter.data.AppDatabase
import com.example.kabaddikounter.data.MatchEntity



class ScoreViewModel(application: Application) : AndroidViewModel(application){
    val teamA = MutableLiveData("Team A")
    val teamB = MutableLiveData("Team B")

    val textExample =  MutableLiveData<String>("test")

    private val _scoreA = MutableLiveData<Int>(0)
    val scoreA: LiveData<Int>
        get() = _scoreA

    private val _scoreB = MutableLiveData<Int>(0)
    val scoreB: LiveData<Int>
        get() = _scoreB

    fun incrementScoreA(points: Int = 1) {
        _scoreA.value = _scoreA.value!! + points
    }

    fun incrementScoreB(points: Int = 1) {
        _scoreB.value = _scoreB.value!! + points
    }

    fun reset() {
        _scoreA.value = 0;
        _scoreB.value = 0;
    }

    private val prefs = ThemePreferences(application)
    // Di ScoreViewModel.kt, ganti:
    val isDarkMode = prefs.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun toggleTheme(isDark: Boolean){
        viewModelScope.launch{
            prefs.saveTheme(isDark)
        }
    }

    // Room Database
    private val db = AppDatabase.getInstance(application)
    private val matchDao = db.matchDao()
    val allMatches = matchDao.getAllMatches()

    // Status simpan untuk feedback ke UI
    private val _saveStatus = MutableLiveData<String?>()
    val saveStatus: LiveData<String?> get() = _saveStatus

    fun saveMatch(title: String) {
        viewModelScope.launch {
            val match = MatchEntity(
                title = title,
                teamAName = teamA.value ?: "Team A",
                teamBName = teamB.value ?: "Team B",
                scoreA = _scoreA.value ?: 0,
                scoreB = _scoreB.value ?: 0
            )
            matchDao.insertMatch(match)
            _saveStatus.postValue("Match \"$title\" berhasil disimpan!")
        }
    }

    fun clearSaveStatus() { _saveStatus.value = null }


    suspend fun getAllMatches(): List<MatchEntity> {
        // akses DAO langsung
         return matchDao.getAllMatchesOnce()
    }
}
