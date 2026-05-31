package com.example.kabaddikounter.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kabaddikounter.MatchListActivity
import com.example.kabaddikounter.repository.MatchRepository

class LiveScoreViewModelFactory(context: Context) : ViewModelProvider.Factory {

    private val repository = MatchRepository(context.applicationContext)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LiveScoreViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LiveScoreViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}