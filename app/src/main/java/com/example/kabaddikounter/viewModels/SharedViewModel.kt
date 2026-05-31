package com.example.kabaddikounter.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {

    // Menyimpan role aktif, null = belum login
    private val _activeRole = MutableLiveData<Role?>(null)
    val activeRole: LiveData<Role?> = _activeRole

    enum class Role { ADMIN, VIEWER }

    fun setRole(role: Role) {
        _activeRole.value = role
    }

    fun clearRole() {
        _activeRole.value = null
    }
}