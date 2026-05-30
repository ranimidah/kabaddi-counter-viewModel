package com.example.kabaddikounter.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * WelcomeViewModel
 *
 * Tanggung jawab:
 * - Menyimpan pilihan role user (ADMIN / VIEWER)
 * - Mengirim event navigasi ke WelcomeFragment via LiveData (SingleEvent)
 *
 * Tidak ada logic navigasi di sini — hanya event yang di-observe Fragment.
 * Ini sesuai pola MVVM: ViewModel tidak tahu tentang NavController.
 */
class WelcomeViewModel : ViewModel() {

    // Enum role yang tersedia
    enum class Role { ADMIN, VIEWER }

    // SingleLiveEvent: event navigasi hanya dikonsumsi SEKALI oleh Fragment
    // Mencegah re-navigate saat rotation/config change
    private val _navigateTo = MutableLiveData<Role?>()
    val navigateTo: LiveData<Role?> = _navigateTo

    /** Dipanggil dari XML via DataBinding: android:onClick="@{() -> viewModel.onAdminSelected()}" */
    fun onAdminSelected() {
        _navigateTo.value = Role.ADMIN
    }

    /** Dipanggil dari XML via DataBinding: android:onClick="@{() -> viewModel.onViewerSelected()}" */
    fun onViewerSelected() {
        _navigateTo.value = Role.VIEWER
    }

    /** Dipanggil Fragment setelah event dikonsumsi agar tidak re-trigger saat rotation */
    fun onNavigationHandled() {
        _navigateTo.value = null
    }
}